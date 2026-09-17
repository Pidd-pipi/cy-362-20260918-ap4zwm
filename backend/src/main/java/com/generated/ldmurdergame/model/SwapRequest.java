package com.generated.ldmurdergame.model;

import java.time.LocalDateTime;

public class SwapRequest {
  private Long id;
  private Long shiftId;
  private Long fromDmId;
  private Long toDmId;
  private String status;
  private String note;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

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

  public Long getFromDmId() {
    return fromDmId;
  }

  public void setFromDmId(Long fromDmId) {
    this.fromDmId = fromDmId;
  }

  public Long getToDmId() {
    return toDmId;
  }

  public void setToDmId(Long toDmId) {
    this.toDmId = toDmId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
