package com.generated.ldmurdergame.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateSwapRequest {
  @NotNull(message = "缺少班次信息")
  private Long shiftId;

  @NotNull(message = "缺少 DM 身份")
  private Long dmId;

  @Size(max = 200, message = "备注不能超过 200 字")
  private String note;

  public Long getShiftId() {
    return shiftId;
  }

  public void setShiftId(Long shiftId) {
    this.shiftId = shiftId;
  }

  public Long getDmId() {
    return dmId;
  }

  public void setDmId(Long dmId) {
    this.dmId = dmId;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }
}
