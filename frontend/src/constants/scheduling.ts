import type { ViewerRole } from "../types";

export const IDENTITY_ROLE_KEY = "ldmurdergame:schedule-role";
export const IDENTITY_DM_KEY = "ldmurdergame:schedule-dm-id";

export const ROLE_MANAGER: ViewerRole = "MANAGER";
export const ROLE_DM: ViewerRole = "DM";

/** 后端返回的班次查看状态 → 标签文案 / 颜色。 */
export const SHIFT_STATUS_META: Record<string, { label: string; type: "primary" | "success" | "info" | "warning" | "danger" }> = {
  CLAIMABLE: { label: "可认领", type: "success" },
  FULL: { label: "已满员", type: "info" },
  SKILL_MISMATCH: { label: "技能不符", type: "warning" },
  SLOT_CONFLICT: { label: "时段冲突", type: "danger" },
  CLAIMED_BY_ME: { label: "我的班次", type: "primary" },
  MANAGER: { label: "店长视角", type: "info" },
};

/** 后端返回的换班单查看状态 → 标签文案 / 颜色。 */
export const SWAP_STATUS_META: Record<string, { label: string; type: "primary" | "success" | "info" | "warning" | "danger" }> = {
  NEED_MY_CONFIRM: { label: "待我确认", type: "warning" },
  WAITING_TARGET: { label: "待对方确认", type: "primary" },
  PENDING: { label: "待处理", type: "info" },
  ACCEPTED: { label: "已确认", type: "success" },
  DECLINED: { label: "已拒绝", type: "danger" },
  CANCELLED: { label: "已撤销", type: "info" },
};

export const SWAP_FILTERS = [
  { value: "PENDING", label: "待处理" },
  { value: "ALL", label: "全部" },
  { value: "ACCEPTED", label: "已确认" },
  { value: "DECLINED", label: "已拒绝" },
  { value: "CANCELLED", label: "已撤销" },
] as const;
