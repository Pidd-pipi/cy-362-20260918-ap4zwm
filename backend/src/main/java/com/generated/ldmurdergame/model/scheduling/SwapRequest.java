package com.generated.ldmurdergame.model.scheduling;

import com.generated.ldmurdergame.model.dm.Dm;

public record SwapRequest(
  Long id,
  Long fromShiftId,
  String fromWorkDate,
  String fromStartTime,
  String requiredSkill,
  Long toShiftId,
  String toWorkDate,
  String toStartTime,
  Dm requester,
  Dm target,
  String status,
  String createdAt,
  /** 当前查看者对该换班单的状态：可承接 / 待对方确认 / 我可确认 / 仅展示等。 */
  String viewerStatus,
  String viewerReason,
  boolean viewerCanAccept,
  boolean viewerCanDecline,
  boolean viewerCanCancel
) {
}
