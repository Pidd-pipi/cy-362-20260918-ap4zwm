package com.generated.ldmurdergame.model.scheduling;

import java.util.List;
import com.generated.ldmurdergame.model.dm.Dm;

public record Shift(
  Long id,
  String workDate,
  String startTime,
  String requiredSkill,
  int requiredCount,
  List<Dm> assignees,
  int assignedCount,
  boolean full,
  /** 当前查看者对该班次的可操作状态，便于前端直接给出满员/冲突原因。 */
  String viewerStatus,
  String viewerReason
) {
}
