<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useScheduling } from "../state/scheduling";
import IdentityBar from "../components/scheduling/IdentityBar.vue";
import CreateShiftForm from "../components/scheduling/CreateShiftForm.vue";
import ShiftCard from "../components/scheduling/ShiftCard.vue";
import MyShiftsPanel from "../components/scheduling/MyShiftsPanel.vue";
import SwapPanel from "../components/scheduling/SwapPanel.vue";
import SwapDialog from "../components/scheduling/SwapDialog.vue";

const schedule = useScheduling();

onMounted(() => {
  schedule.init();
});

const isManager = computed(() => schedule.state.role === "MANAGER");
const noDmSelected = computed(
  () => !isManager.value && schedule.state.board != null && schedule.state.dmId == null);

const swapVisible = ref(false);
const swapShiftId = ref<number | null>(null);

function openSwap(shiftId: number) {
  swapShiftId.value = shiftId;
  swapVisible.value = true;
}

function weekdayLabel(date: string): string {
  const names = ["周日", "周一", "周二", "周三", "周四", "周五", "周六"];
  return names[new Date(`${date}T00:00:00`).getDay()];
}

const myShiftCount = computed(() => schedule.myShifts.value.length);
const pendingCount = computed(() => schedule.pendingSwaps.value.length);
const pendingForMe = computed(
  () => schedule.pendingSwaps.value.filter((swap) => swap.viewerStatus === "NEED_MY_CONFIRM").length);
</script>

<template>
  <main class="scheduling-shell">
    <section class="scheduling-workspace">
      <IdentityBar />

      <el-alert
        v-if="schedule.state.error"
        :title="`无法连接排班服务：${schedule.state.error}。记录由后端持久化，后端恢复后刷新即可。`"
        type="error"
        show-icon
        :closable="false"
        class="offline-alert"
      />

      <el-alert
        v-if="noDmSelected"
        title="请先在上方选择一位 DM 身份，才能认领班次或发起换班。"
        type="warning"
        show-icon
        :closable="false"
        class="offline-alert"
      />

      <section class="summary-strip">
        <article class="summary-card">
          <span>未来两周班次缺口</span>
          <strong :class="{ 'gap-hot': schedule.gapCount.value > 0 }">
            {{ schedule.gapCount.value }}
          </strong>
          <small>个待认领名额</small>
        </article>
        <article v-if="!isManager" class="summary-card">
          <span>我的班次</span>
          <strong>{{ myShiftCount }}</strong>
          <small>近期已认领</small>
        </article>
        <article v-if="!isManager" class="summary-card">
          <span>待处理换班</span>
          <strong :class="{ 'gap-hot': pendingForMe > 0 }">{{ pendingForMe }} / {{ pendingCount }}</strong>
          <small>待我确认 / 全部待处理</small>
        </article>
      </section>

      <div class="scheduling-grid">
        <div class="left-column">
          <CreateShiftForm v-if="isManager" />

          <section class="card-panel shift-board">
            <div class="panel-head">
              <h3>班次缺口（未来 14 天）</h3>
              <span class="section-hint">满员或不能认领时，卡片右侧会说明具体原因</span>
            </div>

            <el-skeleton
              v-if="schedule.state.loading && schedule.state.board == null"
              :rows="6"
              animated
            />

            <el-empty
              v-else-if="schedule.shiftsByDate.value.length === 0"
              description="近期还没有建立任何班次"
            />

            <div
              v-for="group in schedule.shiftsByDate.value"
              :key="group.date"
              class="date-group"
            >
              <h4 class="date-heading">
                {{ group.date }}
                <span class="weekday">{{ weekdayLabel(group.date) }}</span>
              </h4>
              <ShiftCard
                v-for="shift in group.shifts"
                :key="shift.id"
                :shift="shift"
                @request-swap="openSwap"
              />
            </div>
          </section>
        </div>

        <aside class="right-column" v-if="!isManager">
          <MyShiftsPanel @request-swap="openSwap" />
          <SwapPanel />
        </aside>
      </div>
    </section>

    <SwapDialog v-model:visible="swapVisible" :from-shift-id="swapShiftId" />
  </main>
</template>
