package com.generated.ldmurdergame.controller;

import java.time.LocalDate;
import java.util.Map;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.generated.ldmurdergame.model.scheduling.ClaimRequest;
import com.generated.ldmurdergame.model.scheduling.CreateShiftRequest;
import com.generated.ldmurdergame.model.scheduling.CreateSwapRequest;
import com.generated.ldmurdergame.model.scheduling.ScheduleBoard;
import com.generated.ldmurdergame.model.scheduling.SchedulingMeta;
import com.generated.ldmurdergame.model.scheduling.SwapActionRequest;
import com.generated.ldmurdergame.service.SchedulingService;
import com.generated.ldmurdergame.service.SchedulingService.Actor;

/**
 * DM 排班台接口。前端通过 Nginx /api/ 反代，因此同时挂在根路径与 /api 下。
 * 身份通过请求头传递：X-Role=MANAGER 表示店长；DM 携带 X-Dm-Id。
 */
@RestController
@RequestMapping({"", "/api"})
public class SchedulingController {
  private final SchedulingService schedulingService;

  public SchedulingController(SchedulingService schedulingService) {
    this.schedulingService = schedulingService;
  }

  @GetMapping("/scheduling/meta")
  public SchedulingMeta meta() {
    return schedulingService.meta();
  }

  @GetMapping("/scheduling/board")
  public ScheduleBoard board(
      @RequestParam(required = false) String from,
      @RequestParam(required = false) String to,
      @RequestParam(required = false) String swapStatus,
      @RequestHeader(value = "X-Role", required = false) String role,
      @RequestHeader(value = "X-Dm-Id", required = false) String dmId) {
    Actor actor = schedulingService.resolveActor(role, dmId);
    LocalDate fromDate = from == null || from.isBlank() ? null : LocalDate.parse(from);
    LocalDate toDate = to == null || to.isBlank() ? null : LocalDate.parse(to);
    // 缺省只看待处理换班，前端显式传 ALL 时返回全部历史。
    String effectiveSwapStatus = swapStatus == null || swapStatus.isBlank() ? "PENDING" : swapStatus;
    return schedulingService.board(fromDate, toDate, actor, effectiveSwapStatus);
  }

  @PostMapping("/scheduling/shifts")
  public Map<String, String> createShift(
      @RequestHeader(value = "X-Role", required = false) String role,
      @RequestHeader(value = "X-Dm-Id", required = false) String dmId,
      @Valid @RequestBody CreateShiftRequest request) {
    Actor actor = schedulingService.resolveActor(role, dmId);
    String message = schedulingService.createShift(actor, request);
    return Map.of("message", message);
  }

  @PostMapping("/scheduling/shifts/{id}/claim")
  public Map<String, String> claim(
      @PathVariable Long id,
      @RequestHeader(value = "X-Role", required = false) String role,
      @RequestHeader(value = "X-Dm-Id", required = false) String dmId,
      @RequestBody(required = false) ClaimRequest request) {
    Actor actor = schedulingService.resolveActor(role, dmId);
    Long bodyDmId = request == null ? null : request.dmId();
    String message = schedulingService.claimShift(actor, id, bodyDmId);
    return Map.of("message", message);
  }

  @PostMapping("/scheduling/swaps")
  public Map<String, String> createSwap(
      @RequestHeader(value = "X-Role", required = false) String role,
      @RequestHeader(value = "X-Dm-Id", required = false) String dmId,
      @Valid @RequestBody CreateSwapRequest request) {
    Actor actor = schedulingService.resolveActor(role, dmId);
    String message = schedulingService.createSwap(actor, request);
    return Map.of("message", message);
  }

  @PostMapping("/scheduling/swaps/{id}/accept")
  public Map<String, String> acceptSwap(
      @PathVariable Long id,
      @RequestHeader(value = "X-Role", required = false) String role,
      @RequestHeader(value = "X-Dm-Id", required = false) String dmId,
      @RequestBody(required = false) SwapActionRequest request) {
    Actor actor = schedulingService.resolveActor(role, dmId);
    ensureSelf(actor, request == null ? null : request.dmId());
    String message = schedulingService.acceptSwap(actor, id);
    return Map.of("message", message);
  }

  @PostMapping("/scheduling/swaps/{id}/decline")
  public Map<String, String> declineSwap(
      @PathVariable Long id,
      @RequestHeader(value = "X-Role", required = false) String role,
      @RequestHeader(value = "X-Dm-Id", required = false) String dmId,
      @RequestBody(required = false) SwapActionRequest request) {
    Actor actor = schedulingService.resolveActor(role, dmId);
    ensureSelf(actor, request == null ? null : request.dmId());
    String message = schedulingService.declineSwap(actor, id);
    return Map.of("message", message);
  }

  @PostMapping("/scheduling/swaps/{id}/cancel")
  public Map<String, String> cancelSwap(
      @PathVariable Long id,
      @RequestHeader(value = "X-Role", required = false) String role,
      @RequestHeader(value = "X-Dm-Id", required = false) String dmId,
      @RequestBody(required = false) SwapActionRequest request) {
    Actor actor = schedulingService.resolveActor(role, dmId);
    ensureSelf(actor, request == null ? null : request.dmId());
    String message = schedulingService.cancelSwap(actor, id);
    return Map.of("message", message);
  }

  private void ensureSelf(Actor actor, Long bodyDmId) {
    if (bodyDmId != null && (actor.dm() == null || !bodyDmId.equals(actor.dm().id()))) {
      throw new com.generated.ldmurdergame.exception.ApiException("操作身份与所选 DM 不一致，请刷新后重试");
    }
  }
}
