package com.generated.ldmurdergame.controller;

import java.time.LocalDate;
import java.util.Map;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.generated.ldmurdergame.model.BoardResponse;
import com.generated.ldmurdergame.model.CreateShiftRequest;
import com.generated.ldmurdergame.model.CreateSwapRequest;
import com.generated.ldmurdergame.model.DmActionRequest;
import com.generated.ldmurdergame.service.ScheduleService;

@RestController
public class ScheduleController {
  private final ScheduleService scheduleService;

  public ScheduleController(ScheduleService scheduleService) {
    this.scheduleService = scheduleService;
  }

  @GetMapping({"/schedule/board", "/api/schedule/board"})
  public BoardResponse board(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam(required = false) Long dmId) {
    return scheduleService.getBoard(date, dmId);
  }

  @PostMapping({"/schedule/shifts", "/api/schedule/shifts"})
  public Map<String, Object> createShift(@Valid @RequestBody CreateShiftRequest request) {
    long id = scheduleService.createShift(request);
    return Map.of("id", id, "message", "班次创建成功");
  }

  @PostMapping({"/schedule/shifts/{id}/claim", "/api/schedule/shifts/{id}/claim"})
  public Map<String, String> claimShift(@PathVariable long id, @Valid @RequestBody DmActionRequest request) {
    scheduleService.claimShift(id, request.getDmId());
    return Map.of("message", "认领成功，已加入我的班次");
  }

  @PostMapping({"/schedule/swaps", "/api/schedule/swaps"})
  public Map<String, Object> createSwap(@Valid @RequestBody CreateSwapRequest request) {
    long id = scheduleService.createSwapRequest(request.getShiftId(), request.getDmId(), request.getNote());
    return Map.of("id", id, "message", "换班申请已提交，等待同技能 DM 承接");
  }

  @PostMapping({"/schedule/swaps/{id}/accept", "/api/schedule/swaps/{id}/accept"})
  public Map<String, String> acceptSwap(@PathVariable long id, @Valid @RequestBody DmActionRequest request) {
    scheduleService.acceptSwap(id, request.getDmId());
    return Map.of("message", "已承接，等待发起人确认");
  }

  @PostMapping({"/schedule/swaps/{id}/confirm", "/api/schedule/swaps/{id}/confirm"})
  public Map<String, String> confirmSwap(@PathVariable long id, @Valid @RequestBody DmActionRequest request) {
    scheduleService.confirmSwap(id, request.getDmId());
    return Map.of("message", "换班完成，双方排班已更新");
  }

  @PostMapping({"/schedule/swaps/{id}/cancel", "/api/schedule/swaps/{id}/cancel"})
  public Map<String, String> cancelSwap(@PathVariable long id, @Valid @RequestBody DmActionRequest request) {
    scheduleService.cancelSwap(id, request.getDmId());
    return Map.of("message", "操作成功");
  }
}
