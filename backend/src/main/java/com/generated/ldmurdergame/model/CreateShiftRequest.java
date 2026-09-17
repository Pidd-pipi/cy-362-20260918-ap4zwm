package com.generated.ldmurdergame.model;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateShiftRequest {
  @NotNull(message = "请选择排班日期")
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate shiftDate;

  @NotBlank(message = "请选择班次时段")
  private String slot;

  @NotBlank(message = "请选择所需技能")
  private String skill;

  @NotNull(message = "请填写所需人数")
  @Min(value = 1, message = "所需人数至少为 1")
  private Integer requiredCount;

  private String createdBy;

  public LocalDate getShiftDate() {
    return shiftDate;
  }

  public void setShiftDate(LocalDate shiftDate) {
    this.shiftDate = shiftDate;
  }

  public String getSlot() {
    return slot;
  }

  public void setSlot(String slot) {
    this.slot = slot;
  }

  public String getSkill() {
    return skill;
  }

  public void setSkill(String skill) {
    this.skill = skill;
  }

  public Integer getRequiredCount() {
    return requiredCount;
  }

  public void setRequiredCount(Integer requiredCount) {
    this.requiredCount = requiredCount;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }
}
