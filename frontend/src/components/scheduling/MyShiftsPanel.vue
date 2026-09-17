<script setup lang="ts">
import { useScheduling } from "../../state/scheduling";
import type { Shift } from "../../types";

const emit = defineEmits<{ requestSwap: [shiftId: number] }>();

const schedule = useScheduling();

function weekdayLabel(date: string): string {
  const names = ["周日", "周一", "周二", "周三", "周四", "周五", "周六"];
  return names[new Date(`${date}T00:00:00`).getDay()];
}

function shiftKey(shift: Shift): string {
  return `${shift.workDate}-${shift.startTime}`;
}
</script>

<template>
  <section class="card-panel my-shifts">
    <h3>我的班次</h3>
    <el-empty
      v-if="schedule.myShifts.value.length === 0"
      description="近期还没有认领的班次，去下方班次缺口认领吧"
      :image-size="80"
    />
    <ul v-else class="my-shift-list">
      <li v-for="shift in schedule.myShifts.value" :key="shiftKey(shift)" class="my-shift-item">
        <div>
          <strong>{{ shift.workDate }} {{ weekdayLabel(shift.workDate) }} · {{ shift.startTime }}</strong>
          <el-tag size="small" effect="plain" class="skill-tag">{{ shift.requiredSkill }}</el-tag>
        </div>
        <el-button size="small" @click="emit('requestSwap', shift.id)">申请换班</el-button>
      </li>
    </ul>
  </section>
</template>
