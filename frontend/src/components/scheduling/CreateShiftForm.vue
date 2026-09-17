<script setup lang="ts">
import { reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import type { FormInstance, FormRules } from "element-plus";
import { useScheduling } from "../../state/scheduling";

const schedule = useScheduling();

function isoDate(offset: number): string {
  const date = new Date();
  date.setDate(date.getDate() + offset);
  return date.toISOString().slice(0, 10);
}

const formRef = ref<FormInstance>();
const submitting = ref(false);
const form = reactive({
  workDate: isoDate(0),
  startTime: "14:00",
  requiredSkill: "",
  requiredCount: 1,
});

const rules: FormRules = {
  workDate: [{ required: true, message: "请选择班次日期", trigger: "change" }],
  startTime: [{ required: true, message: "仅可建立 14:00 / 19:00 班次", trigger: "change" }],
  requiredSkill: [{ required: true, message: "请选择所需技能", trigger: "change" }],
  requiredCount: [{ required: true, message: "请填写所需人数", trigger: "change" }],
};

async function submit() {
  if (!formRef.value) {
    return;
  }
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) {
    return;
  }
  submitting.value = true;
  try {
    const message = await schedule.addShift({
      workDate: form.workDate,
      startTime: form.startTime,
      requiredSkill: form.requiredSkill,
      requiredCount: Number(form.requiredCount),
    });
    ElMessage.success(message);
    form.requiredCount = 1;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "建班失败");
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <section class="card-panel create-shift">
    <h3>建立班次</h3>
    <p class="section-hint">按日期建立 14:00 / 19:00 两个固定时段的班次，并填写所需技能与人数。</p>
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
      class="create-shift-form"
      @submit.prevent
    >
      <el-form-item label="班次日期" prop="workDate">
        <el-date-picker
          v-model="form.workDate"
          type="date"
          value-format="YYYY-MM-DD"
          :disabled-date="(date: Date) => date.getTime() < new Date(new Date().toDateString()).getTime()"
          placeholder="选择日期"
          class="full-width"
        />
      </el-form-item>
      <el-form-item label="班次时段" prop="startTime">
        <el-radio-group v-model="form.startTime">
          <el-radio-button
            v-for="time in schedule.startTimes.value"
            :key="time"
            :value="time"
          >
            {{ time }}
          </el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="所需技能" prop="requiredSkill">
        <el-select v-model="form.requiredSkill" placeholder="选择技能" class="full-width">
          <el-option v-for="skill in schedule.skills.value" :key="skill" :label="skill" :value="skill" />
        </el-select>
      </el-form-item>
      <el-form-item label="所需人数" prop="requiredCount">
        <el-input-number v-model="form.requiredCount" :min="1" :max="12" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="large" :loading="submitting" @click="submit">
          建立班次
        </el-button>
      </el-form-item>
    </el-form>
  </section>
</template>
