<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { ElMessage } from "element-plus";
import {
  acceptSwap,
  cancelSwap,
  claimShift,
  confirmSwap,
  createShift,
  createSwapRequest,
  fetchScheduleBoard,
} from "../../api/schedule";
import { SCHEDULE_MESSAGES } from "../../constants/messages";
import { loadIdentity, saveIdentity, type Identity } from "../../state/identity";
import type { MyShiftView, ScheduleBoard as BoardData, ShiftGapView } from "../../types";
import MyShiftPanel from "./MyShiftPanel.vue";
import ShiftCreatePanel from "./ShiftCreatePanel.vue";
import ShiftGapPanel from "./ShiftGapPanel.vue";
import SwapPanel from "./SwapPanel.vue";

interface CreateShiftForm {
  shiftDate: string;
  slot: string;
  skill: string;
  requiredCount: number;
}

function todayString(): string {
  const now = new Date();
  const pad = (value: number) => String(value).padStart(2, "0");
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`;
}

const identity = ref<Identity>(loadIdentity());
const activeDate = ref(todayString());
const board = ref<BoardData | null>(null);
const loading = ref(false);
const loadError = ref("");

const isManager = computed(() => identity.value.role === "manager");
const identityValue = computed(() =>
  identity.value.role === "manager" ? "manager" : String(identity.value.dmId),
);
const dms = computed(() => board.value?.dms ?? []);
const shifts = computed(() => board.value?.shifts ?? []);
const myShifts = computed(() => board.value?.myShifts ?? []);
const swaps = computed(() => board.value?.swaps ?? []);
const pendingSwapShiftIds = computed(() =>
  swaps.value
    .filter(
      (swap) =>
        (swap.status === "OPEN" || swap.status === "ACCEPTED") && swap.fromDmId === identity.value.dmId,
    )
    .map((swap) => swap.shiftId),
);

const swapDialogVisible = ref(false);
const swapTarget = ref<MyShiftView | null>(null);
const swapNote = ref("");

async function reload() {
  loading.value = true;
  try {
    const dmId = identity.value.role === "dm" ? identity.value.dmId : null;
    board.value = await fetchScheduleBoard(activeDate.value, dmId);
    loadError.value = "";
    if (identity.value.role === "dm" && !board.value.dms.some((dm) => dm.id === identity.value.dmId)) {
      identity.value = { role: "manager", dmId: null };
      saveIdentity(identity.value);
      ElMessage.warning("所选 DM 已不存在，已切换回店长身份");
    }
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : SCHEDULE_MESSAGES.loadFailed;
  } finally {
    loading.value = false;
  }
}

function onIdentityChange(value: string) {
  identity.value = value === "manager" ? { role: "manager", dmId: null } : { role: "dm", dmId: Number(value) };
  saveIdentity(identity.value);
  void reload();
}

async function runAction(action: () => Promise<{ message: string }>) {
  try {
    const result = await action();
    ElMessage.success(result.message || SCHEDULE_MESSAGES.actionDone);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : SCHEDULE_MESSAGES.actionFailed);
  } finally {
    await reload();
  }
}

function onCreateShift(payload: CreateShiftForm) {
  void (async () => {
    try {
      const result = await createShift({ ...payload, createdBy: "店长" });
      ElMessage.success(result.message || SCHEDULE_MESSAGES.actionDone);
      if (activeDate.value === payload.shiftDate) {
        await reload();
      } else {
        activeDate.value = payload.shiftDate;
      }
    } catch (error) {
      ElMessage.error(error instanceof Error ? error.message : SCHEDULE_MESSAGES.actionFailed);
    }
  })();
}

function onClaim(shift: ShiftGapView) {
  if (identity.value.dmId === null) {
    return;
  }
  const dmId = identity.value.dmId;
  void runAction(() => claimShift(shift.id, dmId));
}

function openSwapDialog(shift: MyShiftView) {
  swapTarget.value = shift;
  swapNote.value = "";
  swapDialogVisible.value = true;
}

function submitSwap() {
  if (swapTarget.value === null || identity.value.dmId === null) {
    return;
  }
  const shiftId = swapTarget.value.shiftId;
  const dmId = identity.value.dmId;
  const note = swapNote.value.trim();
  swapDialogVisible.value = false;
  void runAction(() => createSwapRequest(shiftId, dmId, note));
}

function onAcceptSwap(id: number) {
  if (identity.value.dmId === null) {
    return;
  }
  const dmId = identity.value.dmId;
  void runAction(() => acceptSwap(id, dmId));
}

function onConfirmSwap(id: number) {
  if (identity.value.dmId === null) {
    return;
  }
  const dmId = identity.value.dmId;
  void runAction(() => confirmSwap(id, dmId));
}

function onCancelSwap(id: number) {
  if (identity.value.dmId === null) {
    return;
  }
  const dmId = identity.value.dmId;
  void runAction(() => cancelSwap(id, dmId));
}

watch(activeDate, () => {
  void reload();
});

onMounted(() => {
  void reload();
});
</script>

<template>
  <div class="schedule-board">
    <div class="board-toolbar">
      <div class="toolbar-group">
        <span class="toolbar-label">当前身份</span>
        <el-select :model-value="identityValue" style="width: 240px" @change="onIdentityChange">
          <el-option label="店长（建班与总览）" value="manager" />
          <el-option
            v-for="dm in dms"
            :key="dm.id"
            :label="`${dm.name} · ${dm.skills.join(' / ')}`"
            :value="String(dm.id)"
          />
        </el-select>
      </div>
      <div class="toolbar-group">
        <span class="toolbar-label">排班日期</span>
        <el-date-picker
          v-model="activeDate"
          type="date"
          value-format="YYYY-MM-DD"
          :clearable="false"
          style="width: 160px"
        />
      </div>
      <el-button :loading="loading" @click="reload">刷新</el-button>
    </div>

    <el-alert v-if="loadError" :title="loadError" type="error" :closable="false" class="board-alert" />

    <template v-if="board">
      <ShiftCreatePanel
        v-if="isManager"
        :date="activeDate"
        :slots="board.slots"
        :skills="board.skills"
        @create="onCreateShift"
      />
      <ShiftGapPanel :shifts="shifts" :is-manager="isManager" @claim="onClaim" />
      <div v-if="!isManager" class="board-duo">
        <MyShiftPanel
          :my-shifts="myShifts"
          :pending-swap-shift-ids="pendingSwapShiftIds"
          @request-swap="openSwapDialog"
        />
        <SwapPanel
          :swaps="swaps"
          :viewer-dm-id="identity.dmId"
          @accept="onAcceptSwap"
          @confirm="onConfirmSwap"
          @cancel="onCancelSwap"
        />
      </div>
      <SwapPanel v-else :swaps="swaps" :viewer-dm-id="null" />
    </template>

    <el-dialog v-model="swapDialogVisible" title="发起换班" width="440px">
      <p v-if="swapTarget" class="dialog-tip">
        班次：{{ swapTarget.shiftDate }} {{ swapTarget.slot }} · {{ swapTarget.skill }}
      </p>
      <p class="dialog-tip">提交后需同技能且当日空档的 DM 承接，双方确认后两张排班同时更新。</p>
      <el-input
        v-model="swapNote"
        type="textarea"
        :rows="3"
        maxlength="200"
        show-word-limit
        placeholder="换班原因（选填）"
      />
      <template #footer>
        <el-button @click="swapDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSwap">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>
