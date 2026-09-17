package com.generated.ldmurdergame.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SwapView {
  private Long id;
  private Long shiftId;
  private LocalDate shiftDate;
  private String slot;
  private String skill;
  private Long fromDmId;
  private String fromDmName;
  private Long toDmId;
  private String toDmName;
  private String status;
  private String note;
  private Boolean eligible;
  private String reason;
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

  public Long getFromDmId() {
    return fromDmId;
  }

  public void setFromDmId(Long fromDmId) {
    this.fromDmId = fromDmId;
  }

  public String getFromDmName() {
    return fromDmName;
  }

  public void setFromDmName(String fromDmName) {
    this.fromDmName = fromDmName;
  }

  public Long getToDmId() {
    return toDmId;
  }

  public void setToDmId(Long toDmId) {
    this.toDmId = toDmId;
  }

  public String getToDmName() {
    return toDmName;
  }

  public void setToDmName(String toDmName) {
    this.toDmName = toDmName;
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

  public Boolean getEligible() {
    return eligible;
  }

  public void setEligible(Boolean eligible) {
    this.eligible = eligible;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
