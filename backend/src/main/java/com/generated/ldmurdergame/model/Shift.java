package com.generated.ldmurdergame.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Shift {
  private Long id;
  private LocalDate shiftDate;
  private String slot;
  private String skill;
  private Integer requiredCount;
  private String createdBy;
  private LocalDateTime createdAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

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

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
