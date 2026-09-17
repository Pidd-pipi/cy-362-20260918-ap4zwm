package com.generated.ldmurdergame.model.scheduling;

import java.util.List;
import com.generated.ldmurdergame.model.dm.Dm;

public record SchedulingMeta(
  List<String> skills,
  List<String> startTimes,
  List<Dm> dms
) {
}
