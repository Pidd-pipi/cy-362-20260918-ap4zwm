package com.generated.ldmurdergame.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.generated.ldmurdergame.config.ScheduleRules;
import com.generated.ldmurdergame.exception.ApiException;
import com.generated.ldmurdergame.mapper.DmMapper;
import com.generated.ldmurdergame.mapper.ShiftAssignmentMapper;
import com.generated.ldmurdergame.mapper.ShiftMapper;
import com.generated.ldmurdergame.mapper.SwapRequestMapper;
import com.generated.ldmurdergame.model.AssignmentView;
import com.generated.ldmurdergame.model.BoardResponse;
import com.generated.ldmurdergame.model.CreateShiftRequest;
import com.generated.ldmurdergame.model.Dm;
import com.generated.ldmurdergame.model.MyShiftView;
import com.generated.ldmurdergame.model.Shift;
import com.generated.ldmurdergame.model.ShiftAssignment;
import com.generated.ldmurdergame.model.ShiftView;
import com.generated.ldmurdergame.model.SwapRequest;
import com.generated.ldmurdergame.model.SwapView;
import com.generated.ldmurdergame.model.ViewerState;

@Service
public class ScheduleService {
  private final DmMapper dmMapper;
  private final ShiftMapper shiftMapper;
  private final ShiftAssignmentMapper assignmentMapper;
  private final SwapRequestMapper swapMapper;

  public ScheduleService(DmMapper dmMapper, ShiftMapper shiftMapper,
      ShiftAssignmentMapper assignmentMapper, SwapRequestMapper swapMapper) {
    this.dmMapper = dmMapper;
    this.shiftMapper = shiftMapper;
    this.assignmentMapper = assignmentMapper;
    this.swapMapper = swapMapper;
  }

  public BoardResponse getBoard(LocalDate date, Long dmId) {
    List<Dm> dms = dmMapper.findAll();
    for (Dm dm : dms) {
      dm.setSkills(dmMapper.findSkills(dm.getId()));
    }

    Dm viewer = null;
    if (dmId != null) {
      viewer = dms.stream().filter(dm -> dm.getId().equals(dmId)).findFirst()
          .orElseThrow(() -> new ApiException("DM 不存在，请重新选择身份"));
    }

    List<Shift> shifts = shiftMapper.findByDate(date);
    List<ShiftView> shiftViews = new ArrayList<>();
    for (Shift shift : shifts) {
      List<AssignmentView> assignments = assignmentMapper.findViewsByShift(shift.getId());
      ViewerState viewerState = viewer == null ? null : resolveViewerState(shift, viewer, assignments);
      shiftViews.add(new ShiftView(shift, assignments, viewerState));
    }

    List<MyShiftView> myShifts = viewer == null ? List.of() : assignmentMapper.findMyShifts(viewer.getId());

    List<SwapView> swaps = viewer == null
        ? swapMapper.findBoardSwapsForManager()
        : swapMapper.findBoardSwapsForDm(viewer.getId());
    if (viewer != null) {
      for (SwapView swap : swaps) {
        applySwapEligibility(swap, viewer);
      }
    }

    return new BoardResponse(date, ScheduleRules.SLOTS, ScheduleRules.SKILLS, dms, shiftViews, myShifts, swaps);
  }

  @Transactional
  public long createShift(CreateShiftRequest request) {
    if (!ScheduleRules.SLOTS.contains(request.getSlot())) {
      throw new ApiException("班次时段仅支持 14:00 或 19:00");
    }
    if (!ScheduleRules.SKILLS.contains(request.getSkill())) {
      throw new ApiException("未知技能：" + request.getSkill());
    }
    Shift shift = new Shift();
    shift.setShiftDate(request.getShiftDate());
    shift.setSlot(request.getSlot());
    shift.setSkill(request.getSkill());
    shift.setRequiredCount(request.getRequiredCount());
    shift.setCreatedBy(request.getCreatedBy() == null || request.getCreatedBy().isBlank()
        ? "店长" : request.getCreatedBy().trim());
    shiftMapper.insert(shift);
    return shift.getId();
  }

  @Transactional
  public void claimShift(long shiftId, long dmId) {
    Shift shift = shiftMapper.findByIdForUpdate(shiftId);
    if (shift == null) {
      throw new ApiException("班次不存在或已删除");
    }
    Dm dm = requireDm(dmId);
    if (assignmentMapper.findByShiftAndDm(shiftId, dmId) != null) {
      throw new ApiException("您已认领该班次，请勿重复认领");
    }
    if (!dm.getSkills().contains(shift.getSkill())) {
      throw new ApiException("技能不符：该班次需要「" + shift.getSkill() + "」");
    }
    if (assignmentMapper.countByDmAndSlot(dmId, shift.getShiftDate(), shift.getSlot()) > 0) {
      throw new ApiException("时段冲突：您当日 " + shift.getSlot() + " 已有其他班次");
    }
    if (assignmentMapper.countByShift(shiftId) >= shift.getRequiredCount()) {
      throw new ApiException("该班次已满员");
    }
    ShiftAssignment assignment = new ShiftAssignment();
    assignment.setShiftId(shiftId);
    assignment.setDmId(dmId);
    assignment.setShiftDate(shift.getShiftDate());
    assignment.setSlot(shift.getSlot());
    assignmentMapper.insert(assignment);
  }

  @Transactional
  public long createSwapRequest(long shiftId, long dmId, String note) {
    Shift shift = shiftMapper.findById(shiftId);
    if (shift == null) {
      throw new ApiException("班次不存在或已删除");
    }
    requireDm(dmId);
    if (assignmentMapper.findByShiftAndDm(shiftId, dmId) == null) {
      throw new ApiException("您未上岗该班次，无法发起换班");
    }
    if (swapMapper.countActiveByShiftAndFrom(shiftId, dmId) > 0) {
      throw new ApiException("该班次已有进行中的换班申请");
    }
    SwapRequest request = new SwapRequest();
    request.setShiftId(shiftId);
    request.setFromDmId(dmId);
    request.setStatus(ScheduleRules.SWAP_OPEN);
    request.setNote(note == null || note.isBlank() ? null : note.trim());
    swapMapper.insert(request);
    return request.getId();
  }

  @Transactional
  public void acceptSwap(long swapId, long dmId) {
    SwapRequest request = swapMapper.findByIdForUpdate(swapId);
    if (request == null) {
      throw new ApiException("换班申请不存在");
    }
    if (!ScheduleRules.SWAP_OPEN.equals(request.getStatus())) {
      throw new ApiException("该申请已被承接或已取消");
    }
    if (request.getFromDmId() == dmId) {
      throw new ApiException("不能承接自己发起的换班");
    }
    Shift shift = shiftMapper.findById(request.getShiftId());
    if (shift == null) {
      throw new ApiException("关联班次不存在");
    }
    Dm taker = requireDm(dmId);
    if (!taker.getSkills().contains(shift.getSkill())) {
      throw new ApiException("技能不符：该班次需要「" + shift.getSkill() + "」");
    }
    if (assignmentMapper.countByDmAndSlot(dmId, shift.getShiftDate(), shift.getSlot()) > 0) {
      throw new ApiException("您当日 " + shift.getSlot() + " 已有班次，无法承接");
    }
    if (swapMapper.updateAccepted(swapId, dmId) != 1) {
      throw new ApiException("该申请已被处理，请刷新");
    }
  }

  @Transactional
  public void confirmSwap(long swapId, long dmId) {
    SwapRequest request = swapMapper.findByIdForUpdate(swapId);
    if (request == null) {
      throw new ApiException("换班申请不存在");
    }
    if (request.getFromDmId() != dmId) {
      throw new ApiException("仅换班发起人可确认");
    }
    if (!ScheduleRules.SWAP_ACCEPTED.equals(request.getStatus()) || request.getToDmId() == null) {
      throw new ApiException("当前状态不可确认：需等待承接人接单");
    }
    Shift shift = shiftMapper.findByIdForUpdate(request.getShiftId());
    if (shift == null) {
      throw new ApiException("关联班次不存在，换班失败");
    }
    ShiftAssignment assignment = assignmentMapper.findByShiftAndDmForUpdate(request.getShiftId(), request.getFromDmId());
    if (assignment == null) {
      throw new ApiException("原排班已变更，换班取消");
    }
    Dm taker = requireDm(request.getToDmId());
    if (!taker.getSkills().contains(shift.getSkill())) {
      throw new ApiException("承接人技能已变更，换班失败");
    }
    if (assignmentMapper.countByDmAndSlot(taker.getId(), shift.getShiftDate(), shift.getSlot()) > 0) {
      throw new ApiException("承接人当日同时段已有班次，换班失败");
    }
    if (assignmentMapper.reassign(assignment.getId(), request.getFromDmId(), taker.getId()) != 1) {
      throw new ApiException("排班更新失败，已保持原状");
    }
    if (swapMapper.updateCompleted(swapId) != 1) {
      throw new ApiException("申请状态已变更，已保持原状");
    }
  }

  @Transactional
  public void cancelSwap(long swapId, long dmId) {
    SwapRequest request = swapMapper.findByIdForUpdate(swapId);
    if (request == null) {
      throw new ApiException("换班申请不存在");
    }
    boolean isRequester = request.getFromDmId() == dmId;
    boolean isTaker = request.getToDmId() != null && request.getToDmId() == dmId;
    if (ScheduleRules.SWAP_OPEN.equals(request.getStatus())) {
      if (!isRequester) {
        throw new ApiException("仅发起人可取消该申请");
      }
      if (swapMapper.updateCancelled(swapId) != 1) {
        throw new ApiException("该申请已被处理，请刷新");
      }
      return;
    }
    if (ScheduleRules.SWAP_ACCEPTED.equals(request.getStatus())) {
      if (isRequester) {
        if (swapMapper.updateCancelled(swapId) != 1) {
          throw new ApiException("该申请已被处理，请刷新");
        }
        return;
      }
      if (isTaker) {
        if (swapMapper.updateReopened(swapId) != 1) {
          throw new ApiException("该申请已被处理，请刷新");
        }
        return;
      }
      throw new ApiException("您无权操作该申请");
    }
    throw new ApiException("该申请已结束");
  }

  private ViewerState resolveViewerState(Shift shift, Dm viewer, List<AssignmentView> assignments) {
    boolean mine = assignments.stream().anyMatch(item -> item.getDmId().equals(viewer.getId()));
    if (mine) {
      return new ViewerState("CLAIMED", "已认领该班次");
    }
    if (!viewer.getSkills().contains(shift.getSkill())) {
      return new ViewerState("SKILL_MISMATCH", "技能不符：需要「" + shift.getSkill() + "」");
    }
    if (assignmentMapper.countByDmAndSlot(viewer.getId(), shift.getShiftDate(), shift.getSlot()) > 0) {
      return new ViewerState("SLOT_CONFLICT", "时段冲突：当日 " + shift.getSlot() + " 已有班次");
    }
    if (assignments.size() >= shift.getRequiredCount()) {
      return new ViewerState("FULL", "已满员");
    }
    return new ViewerState("CLAIMABLE", "可认领");
  }

  private void applySwapEligibility(SwapView swap, Dm viewer) {
    if (!ScheduleRules.SWAP_OPEN.equals(swap.getStatus())) {
      swap.setEligible(false);
      return;
    }
    if (swap.getFromDmId().equals(viewer.getId())) {
      swap.setEligible(false);
      swap.setReason("本人发起的申请");
      return;
    }
    if (!viewer.getSkills().contains(swap.getSkill())) {
      swap.setEligible(false);
      swap.setReason("技能不符：需要「" + swap.getSkill() + "」");
      return;
    }
    if (assignmentMapper.countByDmAndSlot(viewer.getId(), swap.getShiftDate(), swap.getSlot()) > 0) {
      swap.setEligible(false);
      swap.setReason("当日该时段已有班次");
      return;
    }
    swap.setEligible(true);
  }

  private Dm requireDm(long dmId) {
    Dm dm = dmMapper.findById(dmId);
    if (dm == null) {
      throw new ApiException("DM 不存在");
    }
    dm.setSkills(dmMapper.findSkills(dmId));
    return dm;
  }
}
