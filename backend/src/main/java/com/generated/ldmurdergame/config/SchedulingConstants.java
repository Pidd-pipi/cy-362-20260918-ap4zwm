package com.generated.ldmurdergame.config;

import java.util.List;

/** DM 排班域常量：门店每日仅开放两个固定时段。 */
public final class SchedulingConstants {
  public static final List<String> START_TIMES = List.of("14:00", "19:00");
  public static final List<String> SKILLS =
      List.of("硬核推理", "情感沉浸", "欢乐机制", "恐怖机制", "阵营对抗");

  public static final String SWAP_PENDING = "PENDING";
  public static final String SWAP_ACCEPTED = "ACCEPTED";
  public static final String SWAP_DECLINED = "DECLINED";
  public static final String SWAP_CANCELLED = "CANCELLED";

  private SchedulingConstants() {
  }
}
