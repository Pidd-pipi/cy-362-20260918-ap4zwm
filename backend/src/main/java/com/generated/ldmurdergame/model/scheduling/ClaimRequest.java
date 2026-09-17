package com.generated.ldmurdergame.model.scheduling;

import jakarta.validation.constraints.NotNull;

public record ClaimRequest(
  @NotNull(message = "缺少 dmId")
  Long dmId
) {
}
