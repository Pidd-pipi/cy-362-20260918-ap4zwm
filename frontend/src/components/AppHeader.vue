<script setup lang="ts">
import { useRoute } from "vue-router";
import { APP_CODE, APP_NAME } from "../constants/app";

const links = [
  { path: "/", label: "运营总览" },
  { path: "/scheduling", label: "DM排班台" },
];

const route = useRoute();

function goHealth() {
  window.location.href = "/api/health";
}
</script>

<template>
  <header class="topbar">
    <div class="brand-block">
      <span class="brand-code">{{ APP_CODE }}</span>
      <h1 class="brand-title">{{ APP_NAME }}</h1>
    </div>
    <nav class="topnav" aria-label="主导航">
      <RouterLink
        v-for="link in links"
        :key="link.path"
        :to="link.path"
        class="topnav-link"
        :class="{ active: route.path === link.path }"
      >
        {{ link.label }}
      </RouterLink>
    </nav>
    <el-button type="primary" @click="goHealth">API Health</el-button>
  </header>
</template>
