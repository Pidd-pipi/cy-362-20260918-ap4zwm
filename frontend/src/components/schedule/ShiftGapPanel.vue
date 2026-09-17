<script setup lang="ts">
import type { ShiftGapView } from "../../types";

defineProps<{ shifts: ShiftGapView[]; isManager: boolean }>();
const emit = defineEmits<{ (event: "claim", shift: ShiftGapView): void }>();

const STATE_TAG: Record<string, { label: string; type: "success" | "info" | "warning" | "danger" }> = {
  CLAIMABLE: { label: "可认领", type: "success" },
  CLAIMED: { label: "已认领", type: "info" },
  FULL: { label: "已满员", type: "danger" },
  SKILL_MISMATCH: { label: "技能不符", type: "warning" },
  SLOT_CONFLICT: { label: "时段冲突", type: "warning" },
};
</script>

<template>
  <section class="work-panel">
    <h2>班次缺口</h2>
    <p class="panel-tip">展示当日各班次的上岗进度与缺口人数；满员或冲突时会标明原因。</p>
    <el-table :data="shifts" style="width: 100%" size="large">
      <el-table-column prop="slot" label="时段" width="90" />
      <el-table-column prop="skill" label="所需技能" min-width="110" />
      <el-table-column label="已上岗" min-width="180">
        <template #default="{ row }: { row: ShiftGapView }">
          <div class="dm-tags">
            <el-tag v-for="item in row.assignments" :key="item.dmId" size="small">{{ item.dmName }}</el-tag>
            <span v-if="row.assignments.length === 0" class="reason-text">暂无人上岗</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="进度" width="100">
        <template #default="{ row }: { row: ShiftGapView }">{{ row.assignedCount }}/{{ row.requiredCount }}</template>
      </el-table-column>
      <el-table-column label="缺口" width="110">
        <template #default="{ row }: { row: ShiftGapView }">
          <span v-if="row.gap > 0" class="gap-text">缺 {{ row.gap }} 人</span>
          <el-tag v-else type="success" size="small">已满员</el-tag>
        </template>
      </el-table-column>
      <el-table-column v-if="!isManager" label="认领" min-width="170">
        <template #default="{ row }: { row: ShiftGapView }">
          <el-button
            v-if="row.viewer && row.viewer.state === 'CLAIMABLE'"
            type="primary"
            size="small"
            @click="emit('claim', row)"
          >
            认领班次
          </el-button>
          <template v-else-if="row.viewer">
            <el-tag :type="STATE_TAG[row.viewer.state].type" size="small">
              {{ STATE_TAG[row.viewer.state].label }}
            </el-tag>
            <div class="reason-text">{{ row.viewer.reason }}</div>
          </template>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="当日暂无班次，店长可先在上方建班" :image-size="80" />
      </template>
    </el-table>
  </section>
</template>
