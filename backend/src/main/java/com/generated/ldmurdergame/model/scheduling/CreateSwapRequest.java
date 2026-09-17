package com.generated.ldmurdergame.model.scheduling;

import jakarta.validation.constraints.NotNull;

public record CreateSwapRequest(
  @NotNull(message = "缺少发起人 dmId")
  Long requesterId,

  @NotNull(message = "缺少承接人 dmId")
  Long targetId,

  @NotNull(message = "缺少要转出的班次")
  Long fromShiftId,

  /** 承接人换出的班次；为空表示空档代班。 */
  Long toShiftId
) {
}
