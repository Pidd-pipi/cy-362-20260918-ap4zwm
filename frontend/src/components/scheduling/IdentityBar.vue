<script setup lang="ts">
import { computed } from "vue";
import { ElMessage } from "element-plus";
import { ROLE_DM, ROLE_MANAGER } from "../../constants/scheduling";
import { useScheduling } from "../../state/scheduling";
import type { Dm } from "../../types";

const schedule = useScheduling();

const role = computed({
  get: () => schedule.state.role,
  set: (value) => {
    void schedule.setRole(value).catch(showError);
  },
});

const dmId = computed<number | undefined>({
  get: () => schedule.state.dmId ?? undefined,
  set: (value) => {
    if (value != null) {
      void schedule.setDm(value).catch(showError);
    }
  },
});

function dmLabel(dm: Dm): string {
  return `${dm.name}（${dm.skills.length ? dm.skills.join("、") : "暂无技能"}）`;
}

function showError(error: unknown) {
  ElMessage.error(error instanceof Error ? error.message : "操作失败");
}

async function refresh() {
  try {
    await schedule.refreshBoard();
  } catch (error) {
    showError(error);
  }
}
</script>

<template>
  <section class="identity-bar card-panel">
    <div class="identity-group">
      <span class="identity-label">当前身份</span>
      <el-radio-group v-model="role" size="large">
        <el-radio-button :value="ROLE_MANAGER">店长（建班）</el-radio-button>
        <el-radio-button :value="ROLE_DM">DM（认领 / 换班）</el-radio-button>
      </el-radio-group>
      <el-select
        v-if="role === ROLE_DM"
        v-model="dmId"
        class="dm-select"
        size="large"
        placeholder="选择 DM 身份"
      >
        <el-option
          v-for="dm in schedule.dms.value"
          :key="dm.id"
          :label="dmLabel(dm)"
          :value="dm.id"
        />
      </el-select>
    </div>
    <div class="identity-side">
      <el-tag :type="schedule.state.online ? 'success' : 'danger'" effect="plain" size="large">
        {{ schedule.state.online ? "后端已联通" : "后端未联通" }}
      </el-tag>
      <span v-if="schedule.state.lastLoadedAt" class="loaded-at">
        最近刷新 {{ schedule.state.lastLoadedAt }}
      </span>
      <el-button size="large" :loading="schedule.state.loading" @click="refresh">刷新</el-button>
    </div>
  </section>
</template>
