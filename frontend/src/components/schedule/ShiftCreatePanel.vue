<script setup lang="ts">
import { reactive, watch } from "vue";
import { ElMessage } from "element-plus";

interface CreateShiftForm {
  shiftDate: string;
  slot: string;
  skill: string;
  requiredCount: number;
}

const props = defineProps<{ date: string; slots: string[]; skills: string[] }>();
const emit = defineEmits<{ (event: "create", payload: CreateShiftForm): void }>();

const form = reactive<CreateShiftForm>({
  shiftDate: props.date,
  slot: props.slots[0] ?? "14:00",
  skill: props.skills[0] ?? "",
  requiredCount: 2,
});

watch(
  () => props.date,
  (value) => {
    form.shiftDate = value;
  },
);

function submit() {
  if (!form.shiftDate || !form.slot || !form.skill) {
    ElMessage.warning("请完整填写日期、时段与所需技能");
    return;
  }
  if (!Number.isInteger(form.requiredCount) || form.requiredCount < 1) {
    ElMessage.warning("所需人数至少为 1");
    return;
  }
  emit("create", { ...form });
}
</script>

<template>
  <section class="work-panel">
    <h2>店长建班</h2>
    <p class="panel-tip">按日期建立 14:00 / 19:00 班次，填写所需技能与人数，DM 认领后实时显示缺口。</p>
    <el-form label-position="top" class="create-form">
      <el-form-item label="排班日期">
        <el-date-picker
          v-model="form.shiftDate"
          type="date"
          value-format="YYYY-MM-DD"
          :clearable="false"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="班次时段">
        <el-radio-group v-model="form.slot">
          <el-radio-button v-for="slot in slots" :key="slot" :value="slot">{{ slot }}</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="所需技能">
        <el-select v-model="form.skill" style="width: 100%">
          <el-option v-for="skill in skills" :key="skill" :label="skill" :value="skill" />
        </el-select>
      </el-form-item>
      <el-form-item label="所需人数">
        <el-input-number v-model="form.requiredCount" :min="1" :max="8" style="width: 100%" />
      </el-form-item>
      <el-form-item label=" ">
        <el-button type="primary" style="width: 100%" @click="submit">创建班次</el-button>
      </el-form-item>
    </el-form>
  </section>
</template>
