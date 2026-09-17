package com.generated.ldmurdergame.model;

import jakarta.validation.constraints.NotNull;

public class DmActionRequest {
  @NotNull(message = "缺少 DM 身份")
  private Long dmId;

  public Long getDmId() {
    return dmId;
  }

  public void setDmId(Long dmId) {
    this.dmId = dmId;
  }
}
