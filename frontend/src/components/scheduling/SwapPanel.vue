<script setup lang="ts">
import { computed } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { SWAP_FILTERS, SWAP_STATUS_META } from "../../constants/scheduling";
import { useScheduling } from "../../state/scheduling";
import type { SwapRequest } from "../../types";

const schedule = useScheduling();

const activeFilter = computed({
  get: () => schedule.state.swapFilter,
  set: (value: string) => {
    void schedule.setSwapFilter(value).catch((error: unknown) => {
      ElMessage.error(error instanceof Error ? error.message : "换班列表加载失败");
    });
  },
});

const swaps = computed(() => schedule.state.board?.swaps ?? []);

function statusMeta(swap: SwapRequest) {
  return SWAP_STATUS_META[swap.viewerStatus] ?? {
    label: swap.status,
    type: "info" as const,
  };
}

function routeText(swap: SwapRequest): string {
  if (swap.toShiftId == null) {
    return `${swap.fromWorkDate} ${swap.fromStartTime}「${swap.requiredSkill}」→ 空档代班`;
  }
  return `${swap.fromWorkDate} ${swap.fromStartTime}「${swap.requiredSkill}」 ↔ ${swap.toWorkDate} ${swap.toStartTime} 对调`;
}

async function runWithConfirm(
  swap: SwapRequest,
  action: "accept" | "decline" | "cancel",
  confirmText: string,
) {
  try {
    if (action !== "cancel") {
      await ElMessageBox.confirm(confirmText, "换班确认", {
        type: action === "accept" ? "warning" : "info",
        confirmButtonText: "确定",
        cancelButtonText: "再想想",
      });
    }
    const message = await schedule.swapAction(swap.id, action);
    ElMessage.success(message);
  } catch (error) {
    if (error === "cancel" || (error instanceof Error && error.message === "cancel")) {
      return;
    }
    ElMessage.error(error instanceof Error ? error.message : "操作失败，排班保持原状");
  }
}
</script>

<template>
  <section class="card-panel swap-panel">
    <div class="panel-head">
      <h3>待处理换班</h3>
      <el-radio-group v-model="activeFilter" size="small">
        <el-radio-button
          v-for="filter in SWAP_FILTERS"
          :key="filter.value"
          :value="filter.value"
        >
          {{ filter.label }}
        </el-radio-button>
      </el-radio-group>
    </div>

    <el-empty
      v-if="swaps.length === 0"
      :description="activeFilter === 'PENDING' ? '暂无待处理的换班申请' : '该状态下暂无换班记录'"
      :image-size="80"
    />

    <ul v-else class="swap-list">
      <li v-for="swap in swaps" :key="swap.id" class="swap-item">
        <div class="swap-topline">
          <el-tag :type="statusMeta(swap).type" effect="light">{{ statusMeta(swap).label }}</el-tag>
          <span class="swap-route">{{ routeText(swap) }}</span>
          <span class="swap-time">{{ swap.createdAt }} 发起</span>
        </div>
        <p class="swap-people">
          <b>{{ swap.requester.name }}</b>
          <span class="muted">（{{ swap.requester.skills.join("、") }}）</span>
          <span class="arrow">→</span>
          <b>{{ swap.target.name }}</b>
          <span class="muted">（{{ swap.target.skills.join("、") }}）</span>
        </p>
        <p class="swap-reason">{{ swap.viewerReason }}</p>
        <div class="swap-actions">
          <el-button
            v-if="swap.viewerCanAccept"
            type="primary"
            size="small"
            @click="runWithConfirm(swap, 'accept', '确认后两张排班将同时更新，是否继续？')"
          >
            确认承接
          </el-button>
          <el-button
            v-if="swap.viewerCanDecline"
            size="small"
            @click="runWithConfirm(swap, 'decline', '拒绝后排班保持原状，是否继续？')"
          >
            拒绝
          </el-button>
          <el-button
            v-if="swap.viewerCanCancel"
            size="small"
            @click="runWithConfirm(swap, 'cancel', '')"
          >
            撤销申请
          </el-button>
        </div>
      </li>
    </ul>
  </section>
</template>
