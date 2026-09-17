package com.generated.ldmurdergame.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import com.generated.ldmurdergame.config.SchedulingConstants;
import com.generated.ldmurdergame.exception.ApiException;
import com.generated.ldmurdergame.model.dm.Dm;
import com.generated.ldmurdergame.model.scheduling.CreateShiftRequest;
import com.generated.ldmurdergame.model.scheduling.CreateSwapRequest;
import com.generated.ldmurdergame.model.scheduling.ScheduleBoard;
import com.generated.ldmurdergame.model.scheduling.SchedulingMeta;
import com.generated.ldmurdergame.model.scheduling.Shift;
import com.generated.ldmurdergame.model.scheduling.SwapRequest;
import com.generated.ldmurdergame.repository.SchedulingRepository;
import com.generated.ldmurdergame.repository.SchedulingRepository.AssignmentRow;
import com.generated.ldmurdergame.repository.SchedulingRepository.AssigneeRow;
import com.generated.ldmurdergame.repository.SchedulingRepository.ShiftRow;
import com.generated.ldmurdergame.repository.SchedulingRepository.SwapRow;

@Service
public class SchedulingService {
  /** 跨实例写操作串行化，配合行锁杜绝满员竞态与同时段重复上岗。 */
  private static final Object WRITE_LOCK = new Object();

  private final SchedulingRepository repository;
  private final TransactionTemplate transactions;

  public SchedulingService(SchedulingRepository repository, TransactionTemplate transactions) {
    this.repository = repository;
    this.transactions = transactions;
  }

  /** 当前操作者：店长或某位 DM，由请求头解析。 */
  public record Actor(boolean manager, Dm dm) {
  }

  public Actor resolveActor(String roleHeader, String dmIdHeader) {
    boolean manager = "MANAGER".equalsIgnoreCase(roleHeader);
    Long dmId = parseLong(dmIdHeader);
    Dm dm = dmId == null ? null : repository.findDm(dmId);
    if (!manager && dm == null) {
      throw new ApiException("请先在页面顶部选择一位 DM 身份");
    }
    return new Actor(manager, dm);
  }

  private <T> T inWriteTransaction(Supplier<T> action) {
    synchronized (WRITE_LOCK) {
      return transactions.execute(status -> action.get());
    }
  }

  private void requireManager(Actor actor) {
    if (!actor.manager()) {
      throw new ApiException("仅店长可建立班次");
    }
  }

  private Dm requireDm(Actor actor) {
    if (actor.dm() == null) {
      throw new ApiException("请先在页面顶部选择一位 DM 身份");
    }
    return actor.dm();
  }

  // ---------- 查询 ----------

  public SchedulingMeta meta() {
    return new SchedulingMeta(SchedulingConstants.SKILLS, SchedulingConstants.START_TIMES,
        repository.findAllDms());
  }

  public ScheduleBoard board(LocalDate from, LocalDate to, Actor actor, String swapStatus) {
    LocalDate start = from != null ? from : LocalDate.now();
    LocalDate end = to != null ? to : LocalDate.now().plusDays(13);
    if (end.isBefore(start)) {
      throw new ApiException("查询区间不合法");
    }

    List<Dm> dms = repository.findAllDms();
    List<ShiftRow> rows = repository.findShiftsBetween(start, end);
    Map<Long, List<Dm>> assignees = new HashMap<>();
    for (AssigneeRow row : repository.findAssigneesBetween(start, end)) {
      assignees.computeIfAbsent(row.shiftId(), k -> new ArrayList<>()).add(row.dm());
    }
    Map<Long, ShiftRow> shiftIndex = new LinkedHashMap<>();
    List<Shift> shifts = new ArrayList<>();
    for (ShiftRow row : rows) {
      shiftIndex.put(row.id(), row);
      shifts.add(toShiftView(row, assignees.getOrDefault(row.id(), List.of()), actor));
    }

    Long participantFilter = actor.manager() ? null : actor.dm().id();
    String statusFilter = "ALL".equalsIgnoreCase(swapStatus) ? null : swapStatus;
    List<SwapRow> swapRows = repository.findSwaps(start, end, participantFilter, statusFilter);
    Map<Long, Dm> dmIndex = new HashMap<>();
    for (Dm dm : dms) {
      dmIndex.put(dm.id(), dm);
    }
    List<SwapRequest> swaps = new ArrayList<>();
    for (SwapRow row : swapRows) {
      swaps.add(toSwapView(row, dmIndex, shiftIndex, actor));
    }

    String role = actor.manager() ? "MANAGER" : "DM";
    return new ScheduleBoard(role, actor.dm() == null ? null : actor.dm().id(), actor.dm(),
        SchedulingConstants.SKILLS, SchedulingConstants.START_TIMES, dms, shifts, swaps);
  }

  private Shift toShiftView(ShiftRow row, List<Dm> assigned, Actor actor) {
    int count = assigned.size();
    boolean full = count >= row.requiredCount();
    String viewerStatus;
    String viewerReason;

    if (actor.manager()) {
      viewerStatus = "MANAGER";
      viewerReason = full ? "已满员" : "缺口 " + (row.requiredCount() - count) + " 人";
    } else {
      Dm dm = actor.dm();
      boolean mine = assigned.stream().anyMatch(item -> item.id().equals(dm.id()));
      if (mine) {
        viewerStatus = "CLAIMED_BY_ME";
        viewerReason = "我已上岗";
      } else if (full) {
        viewerStatus = "FULL";
        viewerReason = "班次已满员（需 " + row.requiredCount() + " 人，已认领 " + count + " 人）";
      } else if (!dm.skills().contains(row.requiredSkill())) {
        viewerStatus = "SKILL_MISMATCH";
        viewerReason = "该班次要求「" + row.requiredSkill() + "」技能，你当前不具备";
      } else if (repository.hasAssignment(dm.id(), row.workDate(), row.startTime())) {
        viewerStatus = "SLOT_CONFLICT";
        viewerReason = "当日 " + row.startTime() + " 你已在另一班次上岗，不能重复排班";
      } else {
        viewerStatus = "CLAIMABLE";
        viewerReason = "未满员且符合技能，可认领";
      }
    }
    return new Shift(row.id(), row.workDate().toString(), row.startTime(), row.requiredSkill(),
        row.requiredCount(), assigned, count, full, viewerStatus, viewerReason);
  }

  private SwapRequest toSwapView(
      SwapRow row, Map<Long, Dm> dmIndex, Map<Long, ShiftRow> shiftIndex, Actor actor) {
    Dm requester = dmIndex.get(row.requesterId());
    Dm target = dmIndex.get(row.targetId());
    String viewerStatus;
    String viewerReason;
    boolean canAccept = false;
    boolean canDecline = false;
    boolean canCancel = false;

    boolean isRequester = actor.dm() != null && actor.dm().id() == row.requesterId();
    boolean isTarget = actor.dm() != null && actor.dm().id() == row.targetId();
    String route = describeSwapRoute(row, shiftIndex, isTarget);

    switch (row.status()) {
      case SchedulingConstants.SWAP_PENDING -> {
        if (isTarget) {
          viewerStatus = "NEED_MY_CONFIRM";
          viewerReason = "「" + safeName(requester) + "」" + route + "，待你确认";
          canAccept = true;
          canDecline = true;
        } else if (isRequester) {
          viewerStatus = "WAITING_TARGET";
          viewerReason = "已发起" + route + "，等待「" + safeName(target) + "」确认";
          canCancel = true;
        } else {
          viewerStatus = "PENDING";
          viewerReason = "等待「" + safeName(target) + "」确认";
        }
      }
      case SchedulingConstants.SWAP_ACCEPTED -> {
        viewerStatus = "ACCEPTED";
        viewerReason = "双方已确认，排班已更新";
      }
      case SchedulingConstants.SWAP_DECLINED -> {
        viewerStatus = "DECLINED";
        viewerReason = "「" + safeName(target) + "」已拒绝，排班保持原状";
      }
      case SchedulingConstants.SWAP_CANCELLED -> {
        viewerStatus = "CANCELLED";
        viewerReason = "发起人已撤销，排班保持原状";
      }
      default -> {
        viewerStatus = row.status();
        viewerReason = "";
      }
    }
    return new SwapRequest(row.id(), row.fromShiftId(),
        row.fromWorkDate().toString(), row.fromStartTime(), row.requiredSkill(),
        row.toShiftId(), row.toWorkDate() == null ? null : row.toWorkDate().toString(),
        row.toStartTime(), requester, target, row.status(), row.createdAt(),
        viewerStatus, viewerReason, canAccept, canDecline, canCancel);
  }

  private String describeSwapRoute(SwapRow row, Map<Long, ShiftRow> shiftIndex, boolean asTarget) {
    String from = row.fromWorkDate() + " " + row.fromStartTime() + "「" + row.requiredSkill() + "」班次";
    if (asTarget) {
      if (row.toShiftId() == null) {
        return "将 " + from + "交给你空档代班";
      }
      ShiftRow to = shiftIndex.get(row.toShiftId());
      String toSkill = to != null ? to.requiredSkill() : row.requiredSkill();
      return "申请与你对调：" + from + " ↔ " + row.toWorkDate() + " " + row.toStartTime()
          + "「" + toSkill + "」";
    }
    if (row.toShiftId() == null) {
      return from + "空档代班申请";
    }
    ShiftRow to = shiftIndex.get(row.toShiftId());
    String toSkill = to != null ? to.requiredSkill() : row.requiredSkill();
    return "对调申请：" + from + " ↔ " + row.toWorkDate() + " " + row.toStartTime()
        + "「" + toSkill + "」";
  }

  private static String safeName(Dm dm) {
    return dm == null ? "未知DM" : dm.name();
  }

  // ---------- 店长建班 ----------

  public String createShift(Actor actor, CreateShiftRequest request) {
    requireManager(actor);
    LocalDate date = LocalDate.parse(request.workDate());
    String startTime = request.startTime();
    String skill = request.requiredSkill() == null ? "" : request.requiredSkill().trim();
    if (date.isBefore(LocalDate.now())) {
      throw new ApiException("不能为过去的日期建立班次");
    }
    if (!SchedulingConstants.START_TIMES.contains(startTime)) {
      throw new ApiException("仅可建立 14:00 或 19:00 班次");
    }
    if (!SchedulingConstants.SKILLS.contains(skill)) {
      throw new ApiException("所需技能需从技能清单中选择：" + String.join("、", SchedulingConstants.SKILLS));
    }
    return inWriteTransaction(() -> {
      if (repository.existsShift(date, startTime, skill)) {
        throw new ApiException(date + " " + startTime + "「" + skill + "」班次已存在，请勿重复建立");
      }
      repository.insertShift(date, startTime, skill, request.requiredCount());
      return "已建立 " + date + " " + startTime + "「" + skill + "」班次，需 "
          + request.requiredCount() + " 人";
    });
  }

  // ---------- DM 认领 ----------

  public String claimShift(Actor actor, Long shiftId, Long bodyDmId) {
    Dm dm = requireDm(actor);
    if (bodyDmId != null && !bodyDmId.equals(dm.id())) {
      throw new ApiException("操作身份与所选 DM 不一致，请刷新后重试");
    }
    if (shiftId == null) {
      throw new ApiException("缺少班次 ID");
    }
    return inWriteTransaction(() -> {
      ShiftRow shift = repository.findShiftForUpdate(shiftId);
      if (shift == null) {
        throw new ApiException("班次不存在或已被店长删除");
      }
      List<AssignmentRow> assignments = repository.findAssignmentsForUpdate(shiftId);
      boolean mine = assignments.stream().anyMatch(a -> a.dmId() == dm.id());
      if (mine) {
        throw new ApiException("你已认领该班次，同一班次不能重复认领");
      }
      if (assignments.size() >= shift.requiredCount()) {
        throw new ApiException("班次已满员（需 " + shift.requiredCount() + " 人，已认领 "
            + assignments.size() + " 人）");
      }
      if (!dm.skills().contains(shift.requiredSkill())) {
        throw new ApiException("认领失败：该班次要求「" + shift.requiredSkill()
            + "」技能，你当前不具备");
      }
      if (repository.hasAssignment(dm.id(), shift.workDate(), shift.startTime())) {
        throw new ApiException("认领失败：当日 " + shift.startTime()
            + " 你已在另一班次上岗，不能重复排班");
      }
      repository.insertAssignment(shiftId, dm.id(), shift.workDate(), shift.startTime());
      return "认领成功：" + shift.workDate() + " " + shift.startTime() + "「"
          + shift.requiredSkill() + "」";
    });
  }

  // ---------- 换班：发起 ----------

  public String createSwap(Actor actor, CreateSwapRequest request) {
    Dm requester = requireDm(actor);
    if (request.requesterId() != null && !request.requesterId().equals(requester.id())) {
      throw new ApiException("操作身份与发起人不一致，请刷新后重试");
    }
    if (request.fromShiftId() == null) {
      throw new ApiException("请选择要转出的班次");
    }
    if (request.targetId() == null) {
      throw new ApiException("请选择承接换班的 DM");
    }
    if (request.targetId().equals(requester.id())) {
      throw new ApiException("承接人不能是自己");
    }
    Dm target = repository.findDm(request.targetId());
    if (target == null) {
      throw new ApiException("承接 DM 不存在");
    }
    return inWriteTransaction(() -> {
      ShiftRow from = repository.findShiftForUpdate(request.fromShiftId());
      if (from == null) {
        throw new ApiException("要转出的班次不存在");
      }
      List<AssignmentRow> fromAssignments = repository.findAssignmentsForUpdate(from.id());
      boolean requesterOnFrom = fromAssignments.stream().anyMatch(a -> a.dmId() == requester.id());
      if (!requesterOnFrom) {
        throw new ApiException("你不在该班次上岗，不能发起换班");
      }
      if (!target.skills().contains(from.requiredSkill())) {
        throw new ApiException("承接失败：「" + target.name() + "」不具备该班次要求的「"
            + from.requiredSkill() + "」技能");
      }
      if (repository.hasAssignment(target.id(), from.workDate(), from.startTime())) {
        throw new ApiException("承接失败：「" + target.name() + "」当日 " + from.startTime()
            + " 已有排班，不是空档");
      }
      if (repository.hasPendingSwapFromShift(requester.id(), from.id())) {
        throw new ApiException("该班次已有一笔待确认的换班申请，请勿重复发起");
      }

      ShiftRow to = null;
      if (request.toShiftId() != null) {
        to = repository.findShiftForUpdate(request.toShiftId());
        if (to == null) {
          throw new ApiException("换出的班次不存在");
        }
        if (to.workDate().equals(from.workDate()) && to.startTime().equals(from.startTime())) {
          throw new ApiException("对调班次不能与转出班次处于同一时段");
        }
        List<AssignmentRow> toAssignments = repository.findAssignmentsForUpdate(to.id());
        boolean targetOnTo = toAssignments.stream().anyMatch(a -> a.dmId() == target.id());
        if (!targetOnTo) {
          throw new ApiException("「" + target.name() + "」并不在所选的换出班次上岗");
        }
        if (!requester.skills().contains(to.requiredSkill())) {
          throw new ApiException("对调失败：换出的班次要求「" + to.requiredSkill()
              + "」技能，你当前不具备");
        }
        if (repository.hasAssignment(requester.id(), to.workDate(), to.startTime())) {
          throw new ApiException("对调失败：你当日 " + to.startTime() + " 已有排班，无法承接");
        }
      }
      repository.insertSwap(from.id(), from.workDate(), from.startTime(), from.requiredSkill(),
          to == null ? null : to.id(),
          to == null ? null : to.workDate(),
          to == null ? null : to.startTime(),
          requester.id(), target.id());
      String action = to == null ? "空档代班" : "对调换班";
      return "换班申请已发起（" + action + "），等待「" + target.name() + "」确认";
    });
  }

  // ---------- 换班：确认 / 拒绝 / 撤销 ----------

  public String acceptSwap(Actor actor, Long swapId) {
    Dm operator = requireDm(actor);
    return inWriteTransaction(() -> {
      SwapRow swap = loadPendingSwap(swapId);
      if (swap.targetId() != operator.id()) {
        throw new ApiException("仅承接人本人可以确认换班");
      }
      ShiftRow from = repository.findShiftForUpdate(swap.fromShiftId());
      if (from == null) {
        throw new ApiException("转出班次已不存在，换班无法继续");
      }
      List<AssignmentRow> fromAssignments = repository.findAssignmentsForUpdate(from.id());
      boolean requesterStillOn = fromAssignments.stream()
          .anyMatch(a -> a.dmId() == swap.requesterId());
      if (!requesterStillOn) {
        throw new ApiException("发起人的排班已发生变化，换班无法继续，排班保持原状");
      }
      Dm target = repository.findDm(swap.targetId());
      Dm requester = repository.findDm(swap.requesterId());
      if (target == null || requester == null) {
        throw new ApiException("换班涉及的 DM 已不存在");
      }
      if (!target.skills().contains(from.requiredSkill())) {
        throw new ApiException("你不具备该班次要求的「" + from.requiredSkill() + "」技能");
      }
      if (repository.hasAssignment(target.id(), from.workDate(), from.startTime())) {
        throw new ApiException("确认失败：你当日 " + from.startTime() + " 已有排班，不是空档");
      }

      if (swap.toShiftId() == null) {
        // 空档代班：发起人退出，承接人补入。
        repository.deleteAssignment(from.id(), requester.id());
        repository.insertAssignment(from.id(), target.id(), from.workDate(), from.startTime());
      } else {
        ShiftRow to = repository.findShiftForUpdate(swap.toShiftId());
        if (to == null) {
          throw new ApiException("换出的班次已不存在，换班无法继续");
        }
        List<AssignmentRow> toAssignments = repository.findAssignmentsForUpdate(to.id());
        boolean targetStillOnTo = toAssignments.stream()
            .anyMatch(a -> a.dmId() == target.id());
        if (!targetStillOnTo) {
          throw new ApiException("你已不在换出的班次上岗，对调无法继续，排班保持原状");
        }
        if (!requester.skills().contains(to.requiredSkill())) {
          throw new ApiException("换出的班次要求「" + to.requiredSkill() + "」技能，发起人不具备");
        }
        if (repository.hasAssignment(requester.id(), to.workDate(), to.startTime())) {
          throw new ApiException("发起人当日 " + to.startTime() + " 已有排班，对调无法继续");
        }
        // 两张排班同时更新：先删除两条旧记录，再插入两条新记录。
        repository.deleteAssignment(from.id(), requester.id());
        repository.deleteAssignment(to.id(), target.id());
        repository.insertAssignment(from.id(), target.id(), from.workDate(), from.startTime());
        repository.insertAssignment(to.id(), requester.id(), to.workDate(), to.startTime());
      }
      repository.updateSwapStatus(swap.id(), SchedulingConstants.SWAP_ACCEPTED);
      return "已确认换班，两张排班已同时更新";
    });
  }

  public String declineSwap(Actor actor, Long swapId) {
    Dm operator = requireDm(actor);
    return inWriteTransaction(() -> {
      SwapRow swap = loadPendingSwap(swapId);
      if (swap.targetId() != operator.id()) {
        throw new ApiException("仅承接人本人可以拒绝换班");
      }
      repository.updateSwapStatus(swap.id(), SchedulingConstants.SWAP_DECLINED);
      return "已拒绝该换班申请，排班保持原状";
    });
  }

  public String cancelSwap(Actor actor, Long swapId) {
    Dm operator = requireDm(actor);
    return inWriteTransaction(() -> {
      SwapRow swap = loadPendingSwap(swapId);
      if (swap.requesterId() != operator.id()) {
        throw new ApiException("仅发起人本人可以撤销换班申请");
      }
      repository.updateSwapStatus(swap.id(), SchedulingConstants.SWAP_CANCELLED);
      return "已撤销该换班申请，排班保持原状";
    });
  }

  private SwapRow loadPendingSwap(Long swapId) {
    if (swapId == null) {
      throw new ApiException("缺少换班单 ID");
    }
    SwapRow swap = repository.findSwapForUpdate(swapId);
    if (swap == null) {
      throw new ApiException("换班单不存在");
    }
    if (!SchedulingConstants.SWAP_PENDING.equals(swap.status())) {
      throw new ApiException("该换班单已处理（" + swapStatusLabel(swap.status())
          + "），不能重复操作");
    }
    return swap;
  }

  private String swapStatusLabel(String status) {
    return switch (status) {
      case SchedulingConstants.SWAP_ACCEPTED -> "双方已确认";
      case SchedulingConstants.SWAP_DECLINED -> "已拒绝";
      case SchedulingConstants.SWAP_CANCELLED -> "已撤销";
      default -> status;
    };
  }

  private static Long parseLong(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    try {
      return Long.parseLong(raw.trim());
    } catch (NumberFormatException exception) {
      throw new ApiException("DM 身份标识不合法");
    }
  }
}
