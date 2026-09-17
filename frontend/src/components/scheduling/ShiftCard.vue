<script setup lang="ts">
import { computed, ref } from "vue";
import { ElMessage } from "element-plus";
import { SHIFT_STATUS_META } from "../../constants/scheduling";
import { useScheduling } from "../../state/scheduling";
import type { Shift } from "../../types";

const props = defineProps<{ shift: Shift }>();
const emit = defineEmits<{ requestSwap: [shiftId: number] }>();

const schedule = useScheduling();

const claiming = ref(false);

const statusMeta = computed(() =>
  SHIFT_STATUS_META[props.shift.viewerStatus] ?? {
    label: props.shift.viewerStatus,
    type: "info" as const,
  });

const gap = computed(() =>
  Math.max(0, props.shift.requiredCount - props.shift.assignedCount));

async function claim() {
  claiming.value = true;
  try {
    ElMessage.success(await schedule.claim(props.shift.id));
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "认领失败");
  } finally {
    claiming.value = false;
  }
}
</script>

<template>
  <article class="shift-card" :class="{ 'is-full': shift.full }">
    <div class="shift-main">
      <div class="shift-time">
        <strong>{{ shift.startTime }}</strong>
        <el-tag size="small" effect="plain">{{ shift.requiredSkill }}</el-tag>
      </div>
      <div class="shift-people">
        <el-progress
          :percentage="Math.min(100, Math.round((shift.assignedCount / shift.requiredCount) * 100))"
          :stroke-width="8"
          :show-text="false"
          :status="shift.full ? 'success' : undefined"
        />
        <span class="people-line">
          已上岗 <b>{{ shift.assignedCount }}</b> / {{ shift.requiredCount }} 人
          <span v-if="gap > 0" class="gap-text">· 缺口 {{ gap }} 人</span>
          <span v-else class="full-text">· 已满员</span>
        </span>
        <span class="assignee-line">
          <template v-if="shift.assignees.length">
            DM：
            <el-tag
              v-for="dm in shift.assignees"
              :key="dm.id"
              size="small"
              :type="shift.viewerStatus === 'CLAIMED_BY_ME' && schedule.state.dmId === dm.id ? 'primary' : 'info'"
              class="assignee-tag"
            >
              {{ dm.name }}
            </el-tag>
          </template>
          <span v-else class="muted">尚无人认领</span>
        </span>
      </div>
    </div>
    <div class="shift-action">
      <el-tag :type="statusMeta.type" effect="light">{{ statusMeta.label }}</el-tag>
      <p class="reason-text">{{ shift.viewerReason }}</p>
      <template v-if="schedule.state.role === 'DM'">
        <el-button
          v-if="shift.viewerStatus === 'CLAIMABLE'"
          type="primary"
          :loading="claiming"
          @click="claim"
        >
          认领班次
        </el-button>
        <el-button
          v-else-if="shift.viewerStatus === 'CLAIMED_BY_ME'"
          @click="emit('requestSwap', shift.id)"
        >
          申请换班
        </el-button>
      </template>
    </div>
  </article>
</template>
