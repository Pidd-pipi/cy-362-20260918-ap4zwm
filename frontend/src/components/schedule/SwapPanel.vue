<script setup lang="ts">
import { computed } from "vue";
import type { SwapRequestView } from "../../types";

const props = defineProps<{ swaps: SwapRequestView[]; viewerDmId: number | null }>();
const emit = defineEmits<{
  (event: "accept", id: number): void;
  (event: "confirm", id: number): void;
  (event: "cancel", id: number): void;
}>();

const STATUS_TAG: Record<string, { label: string; type: "success" | "info" | "warning" | "primary" }> = {
  OPEN: { label: "待承接", type: "warning" },
  ACCEPTED: { label: "待发起人确认", type: "primary" },
  COMPLETED: { label: "已完成", type: "success" },
  CANCELLED: { label: "已取消", type: "info" },
};

const pending = computed(() =>
  props.swaps.filter((swap) => {
    if (swap.status !== "OPEN" && swap.status !== "ACCEPTED") {
      return false;
    }
    if (props.viewerDmId === null) {
      return true;
    }
    return swap.status === "OPEN" || swap.fromDmId === props.viewerDmId || swap.toDmId === props.viewerDmId;
  }),
);

const history = computed(() =>
  props.swaps.filter((swap) => swap.status === "COMPLETED" || swap.status === "CANCELLED"),
);

function isMine(swap: SwapRequestView): boolean {
  return props.viewerDmId !== null && swap.fromDmId === props.viewerDmId;
}

function isTaker(swap: SwapRequestView): boolean {
  return props.viewerDmId !== null && swap.toDmId === props.viewerDmId;
}
</script>

<template>
  <section class="work-panel">
    <h2>待处理换班</h2>
    <p class="panel-tip">承接需满足同技能且当日该时段空档；双方确认后两张排班同时更新。</p>
    <el-table :data="pending" style="width: 100%" size="large">
      <el-table-column label="班次" min-width="170">
        <template #default="{ row }: { row: SwapRequestView }">
          {{ row.shiftDate }} {{ row.slot }} · {{ row.skill }}
        </template>
      </el-table-column>
      <el-table-column label="发起人" width="90">
        <template #default="{ row }: { row: SwapRequestView }">{{ row.fromDmName }}</template>
      </el-table-column>
      <el-table-column label="承接人" width="90">
        <template #default="{ row }: { row: SwapRequestView }">{{ row.toDmName ?? "—" }}</template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }: { row: SwapRequestView }">
          <el-tag :type="STATUS_TAG[row.status].type" size="small">{{ STATUS_TAG[row.status].label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" min-width="110">
        <template #default="{ row }: { row: SwapRequestView }">{{ row.note ?? "—" }}</template>
      </el-table-column>
      <el-table-column v-if="viewerDmId !== null" label="操作" min-width="180">
        <template #default="{ row }: { row: SwapRequestView }">
          <template v-if="row.status === 'OPEN'">
            <el-button v-if="isMine(row)" size="small" type="danger" plain @click="emit('cancel', row.id)">
              取消申请
            </el-button>
            <el-button v-else-if="row.eligible" size="small" type="primary" @click="emit('accept', row.id)">
              承接
            </el-button>
            <span v-else class="reason-text">{{ row.reason }}</span>
          </template>
          <template v-else-if="row.status === 'ACCEPTED'">
            <template v-if="isMine(row)">
              <el-button size="small" type="success" @click="emit('confirm', row.id)">确认换班</el-button>
              <el-button size="small" type="danger" plain @click="emit('cancel', row.id)">取消</el-button>
            </template>
            <el-button v-else-if="isTaker(row)" size="small" type="warning" plain @click="emit('cancel', row.id)">
              撤回承接
            </el-button>
            <span v-else class="reason-text">等待双方确认</span>
          </template>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无待处理的换班申请" :image-size="80" />
      </template>
    </el-table>

    <div v-if="history.length > 0" class="swap-history">
      <h3>换班记录</h3>
      <el-table :data="history" style="width: 100%" size="small">
        <el-table-column label="班次" min-width="170">
          <template #default="{ row }: { row: SwapRequestView }">
            {{ row.shiftDate }} {{ row.slot }} · {{ row.skill }}
          </template>
        </el-table-column>
        <el-table-column label="发起人" width="90">
          <template #default="{ row }: { row: SwapRequestView }">{{ row.fromDmName }}</template>
        </el-table-column>
        <el-table-column label="承接人" width="90">
          <template #default="{ row }: { row: SwapRequestView }">{{ row.toDmName ?? "—" }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }: { row: SwapRequestView }">
            <el-tag :type="STATUS_TAG[row.status].type" size="small">{{ STATUS_TAG[row.status].label }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </section>
</template>
