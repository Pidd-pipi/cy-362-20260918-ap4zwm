<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import { ElMessage } from "element-plus";
import { useScheduling } from "../../state/scheduling";
import type { Shift } from "../../types";

const props = defineProps<{
  visible: boolean;
  fromShiftId: number | null;
}>();

const emit = defineEmits<{
  "update:visible": [value: boolean];
}>();

const schedule = useScheduling();

function dmLabel(dm: { name: string; skills: readonly string[] }): string {
  return `${dm.name}（${dm.skills.length ? dm.skills.join("、") : "暂无技能"}）`;
}
const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit("update:visible", value),
});

const mode = ref<"cover" | "exchange">("cover");
const targetId = ref<number | null>(null);
const toShiftId = ref<number | null>(null);
const submitting = ref(false);

const fromShift = computed<Shift | null>(() => {
  if (props.fromShiftId == null || !schedule.state.board) {
    return null;
  }
  return schedule.state.board.shifts.find((shift) => shift.id === props.fromShiftId) ?? null;
});

/** 可选承接人：排除自己（技能/空档是否符合由后端判定并返回原因）。 */
const targetOptions = computed(() =>
  schedule.dms.value.filter((dm) => dm.id !== schedule.state.dmId));

/** 对调模式下，承接人当前已上岗的班次（且不能是同时段）。 */
const targetShiftOptions = computed(() => {
  if (!schedule.state.board || targetId.value == null) {
    return [];
  }
  return schedule.state.board.shifts.filter((shift) =>
    shift.assignees.some((dm) => dm.id === targetId.value)
    && !(fromShift.value
      && shift.workDate === fromShift.value.workDate
      && shift.startTime === fromShift.value.startTime));
});

watch(() => props.visible, (visible) => {
  if (visible) {
    mode.value = "cover";
    targetId.value = null;
    toShiftId.value = null;
  }
});

watch(targetId, () => {
  // 承接人变化后，原先选的对调班次可能已不属于该 DM
  if (toShiftId.value != null
      && !targetShiftOptions.value.some((shift) => shift.id === toShiftId.value)) {
    toShiftId.value = null;
  }
});

async function submit() {
  if (props.fromShiftId == null || schedule.state.dmId == null) {
    return;
  }
  if (targetId.value == null) {
    ElMessage.warning("请选择承接换班的 DM");
    return;
  }
  if (mode.value === "exchange" && toShiftId.value == null) {
    ElMessage.warning("对调换班请选择承接人换出的班次");
    return;
  }
  submitting.value = true;
  try {
    const message = await schedule.requestSwap({
      requesterId: schedule.state.dmId,
      targetId: targetId.value,
      fromShiftId: props.fromShiftId,
      toShiftId: mode.value === "exchange" ? toShiftId.value : null,
    });
    ElMessage.success(message);
    dialogVisible.value = false;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "换班申请失败，排班保持原状");
  } finally {
    submitting.value = false;
  }
}

function shiftLabel(shift: Shift): string {
  return `${shift.workDate} ${shift.startTime}「${shift.requiredSkill}」`;
}
</script>

<template>
  <el-dialog v-model="dialogVisible" title="申请换班" width="520px" destroy-on-close>
    <div v-if="fromShift" class="swap-dialog-body">
      <el-alert
        :closable="false"
        type="info"
        :title="`转出班次：${shiftLabel(fromShift)}`"
        description="承接人须具备该班次所需技能，且当日同一时段为空档；对调时双方还须符合对方班次的技能要求。"
        class="swap-alert"
      />
      <el-form label-position="top">
        <el-form-item label="换班方式" required>
          <el-radio-group v-model="mode">
            <el-radio value="cover">空档代班（对方当日空档，我退出后由其补入）</el-radio>
            <el-radio value="exchange">对调换班（用对方名下另一班次交换）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="承接人（同技能、当日空档）" required>
          <el-select v-model="targetId" placeholder="选择承接 DM" class="full-width">
            <el-option
              v-for="dm in targetOptions"
              :key="dm.id"
              :label="dmLabel(dm)"
              :value="dm.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="mode === 'exchange'" label="承接人换出的班次" required>
          <el-select v-model="toShiftId" placeholder="选择对方名下班次" class="full-width">
            <el-option
              v-for="shift in targetShiftOptions"
              :key="shift.id"
              :label="shiftLabel(shift)"
              :value="shift.id"
              :disabled="!!(fromShift
                && shift.workDate === fromShift.workDate
                && shift.startTime === fromShift.startTime)"
            />
          </el-select>
          <p v-if="targetId != null && targetShiftOptions.length === 0" class="form-hint warn">
            该 DM 名下暂无可用于对调的其他班次，可改用「空档代班」。
          </p>
        </el-form-item>
      </el-form>
      <p class="form-hint">提交后进入「待确认」，由对方确认；任一步失败，两张排班都保持原状。</p>
    </div>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">发起换班</el-button>
    </template>
  </el-dialog>
</template>
