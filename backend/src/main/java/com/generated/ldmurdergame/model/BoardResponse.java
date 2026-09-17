package com.generated.ldmurdergame.model;

import java.time.LocalDate;
import java.util.List;

public class BoardResponse {
  private LocalDate date;
  private List<String> slots;
  private List<String> skills;
  private List<Dm> dms;
  private List<ShiftView> shifts;
  private List<MyShiftView> myShifts;
  private List<SwapView> swaps;

  public BoardResponse(LocalDate date, List<String> slots, List<String> skills, List<Dm> dms,
      List<ShiftView> shifts, List<MyShiftView> myShifts, List<SwapView> swaps) {
    this.date = date;
    this.slots = slots;
    this.skills = skills;
    this.dms = dms;
    this.shifts = shifts;
    this.myShifts = myShifts;
    this.swaps = swaps;
  }

  public LocalDate getDate() {
    return date;
  }

  public List<String> getSlots() {
    return slots;
  }

  public List<String> getSkills() {
    return skills;
  }

  public List<Dm> getDms() {
    return dms;
  }

  public List<ShiftView> getShifts() {
    return shifts;
  }

  public List<MyShiftView> getMyShifts() {
    return myShifts;
  }

  public List<SwapView> getSwaps() {
    return swaps;
  }
}
