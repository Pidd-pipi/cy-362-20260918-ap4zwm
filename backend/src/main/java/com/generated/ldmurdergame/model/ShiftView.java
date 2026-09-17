package com.generated.ldmurdergame.model;

import java.time.LocalDate;
import java.util.List;

public class ShiftView {
  private Long id;
  private LocalDate shiftDate;
  private String slot;
  private String skill;
  private Integer requiredCount;
  private Integer assignedCount;
  private Integer gap;
  private Boolean full;
  private List<AssignmentView> assignments;
  private ViewerState viewer;

  public ShiftView() {
  }

  public ShiftView(Shift shift, List<AssignmentView> assignments, ViewerState viewer) {
    this.id = shift.getId();
    this.shiftDate = shift.getShiftDate();
    this.slot = shift.getSlot();
    this.skill = shift.getSkill();
    this.requiredCount = shift.getRequiredCount();
    this.assignedCount = assignments.size();
    this.gap = Math.max(0, shift.getRequiredCount() - this.assignedCount);
    this.full = this.assignedCount >= shift.getRequiredCount();
    this.assignments = assignments;
    this.viewer = viewer;
  }

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

  public Integer getAssignedCount() {
    return assignedCount;
  }

  public void setAssignedCount(Integer assignedCount) {
    this.assignedCount = assignedCount;
  }

  public Integer getGap() {
    return gap;
  }

  public void setGap(Integer gap) {
    this.gap = gap;
  }

  public Boolean getFull() {
    return full;
  }

  public void setFull(Boolean full) {
    this.full = full;
  }

  public List<AssignmentView> getAssignments() {
    return assignments;
  }

  public void setAssignments(List<AssignmentView> assignments) {
    this.assignments = assignments;
  }

  public ViewerState getViewer() {
    return viewer;
  }

  public void setViewer(ViewerState viewer) {
    this.viewer = viewer;
  }
}
