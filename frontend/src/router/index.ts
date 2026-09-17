import { createRouter, createWebHistory } from "vue-router";
import OverviewPage from "../pages/OverviewPage.vue";
import SchedulingPage from "../pages/SchedulingPage.vue";

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/", name: "overview", component: OverviewPage },
    { path: "/scheduling", name: "scheduling", component: SchedulingPage },
    { path: "/:pathMatch(.*)*", redirect: "/" },
  ],
});
