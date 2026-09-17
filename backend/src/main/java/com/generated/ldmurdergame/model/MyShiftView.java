package com.generated.ldmurdergame.model;

import java.time.LocalDate;

public class MyShiftView {
  private Long assignmentId;
  private Long shiftId;
  private LocalDate shiftDate;
  private String slot;
  private String skill;
  private Integer requiredCount;
  private Integer assignedCount;

  public Long getAssignmentId() {
    return assignmentId;
  }

  public void setAssignmentId(Long assignmentId) {
    this.assignmentId = assignmentId;
  }

  public Long getShiftId() {
    return shiftId;
  }

  public void setShiftId(Long shiftId) {
    this.shiftId = shiftId;
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

  public Integer getAssignedCount() {
    return assignedCount;
  }

  public void setAssignedCount(Integer assignedCount) {
    this.assignedCount = assignedCount;
  }
}
