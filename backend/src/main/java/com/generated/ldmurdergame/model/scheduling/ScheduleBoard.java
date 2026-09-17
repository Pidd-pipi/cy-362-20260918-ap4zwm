package com.generated.ldmurdergame.model.scheduling;

import java.util.List;
import com.generated.ldmurdergame.model.dm.Dm;

public record ScheduleBoard(
  String role,
  Long currentDmId,
  Dm currentDm,
  List<String> skills,
  List<String> startTimes,
  List<Dm> dms,
  List<Shift> shifts,
  List<SwapRequest> swaps
) {
}
