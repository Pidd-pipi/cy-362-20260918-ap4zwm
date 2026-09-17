package com.generated.ldmurdergame.model.scheduling;

import jakarta.validation.constraints.NotNull;

public record SwapActionRequest(
  @NotNull(message = "缺少操作人 dmId")
  Long dmId
) {
}
