package com.generated.ldmurdergame.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ShiftAssignment {
  private Long id;
  private Long shiftId;
  private Long dmId;
  private LocalDate shiftDate;
  private String slot;
  private LocalDateTime createdAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

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

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
