<script setup lang="ts">
import type { MyShiftView } from "../../types";

const props = defineProps<{ myShifts: MyShiftView[]; pendingSwapShiftIds: number[] }>();
const emit = defineEmits<{ (event: "requestSwap", shift: MyShiftView): void }>();

function hasPendingSwap(shiftId: number): boolean {
  return props.pendingSwapShiftIds.includes(shiftId);
}
</script>

<template>
  <section class="work-panel">
    <h2>我的班次</h2>
    <p class="panel-tip">已认领的班次；临时有事可发起换班，由同技能且当日空档的 DM 承接。</p>
    <el-table :data="myShifts" style="width: 100%" size="large">
      <el-table-column prop="shiftDate" label="日期" width="120" />
      <el-table-column prop="slot" label="时段" width="80" />
      <el-table-column prop="skill" label="技能" min-width="100" />
      <el-table-column label="岗位进度" width="100">
        <template #default="{ row }: { row: MyShiftView }">{{ row.assignedCount }}/{{ row.requiredCount }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }: { row: MyShiftView }">
          <el-tooltip v-if="hasPendingSwap(row.shiftId)" content="该班次已有进行中的换班申请" placement="top">
            <span>
              <el-button size="small" disabled>换班中</el-button>
            </span>
          </el-tooltip>
          <el-button v-else size="small" @click="emit('requestSwap', row)">发起换班</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无班次，可先在「班次缺口」中认领" :image-size="80" />
      </template>
    </el-table>
  </section>
</template>
