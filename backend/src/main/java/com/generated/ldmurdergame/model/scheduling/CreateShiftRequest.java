package com.generated.ldmurdergame.model.scheduling;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateShiftRequest(
  @NotBlank(message = "班次日期不能为空")
  @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "日期格式应为 yyyy-MM-dd")
  String workDate,

  @NotBlank(message = "班次时段不能为空（仅可建立 14:00 或 19:00 班次）")
  @Pattern(regexp = "14:00|19:00", message = "仅可建立 14:00 或 19:00 班次")
  String startTime,

  @NotBlank(message = "所需技能不能为空")
  String requiredSkill,

  @NotNull(message = "所需人数不能为空")
  @Min(value = 1, message = "所需人数至少为 1")
  @Max(value = 12, message = "单班所需人数不能超过 12")
  Integer requiredCount
) {
}
