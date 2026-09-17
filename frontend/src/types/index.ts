export interface FeatureItem {
  id: number;
  title: string;
  description: string;
  status: string;
  metric: string;
}

export interface KpiItem {
  label: string;
  value: string;
  trend: string;
  tone: string;
}

export interface OperationRecord {
  key: string;
  name: string;
  owner: string;
  status: string;
  metric: string;
  priority: string;
}

export interface OverviewResponse {
  appName: string;
  appCode: string;
  description: string;
  features: FeatureItem[];
  kpis: KpiItem[];
  records: OperationRecord[];
}

// ---------- DM 排班台 ----------

export type ViewerRole = "MANAGER" | "DM";

export interface Dm {
  id: number;
  name: string;
  skills: readonly string[];
}

/** viewerStatus: MANAGER / CLAIMABLE / FULL / SKILL_MISMATCH / SLOT_CONFLICT / CLAIMED_BY_ME */
export interface Shift {
  id: number;
  workDate: string;
  startTime: string;
  requiredSkill: string;
  requiredCount: number;
  assignees: readonly Dm[];
  assignedCount: number;
  full: boolean;
  viewerStatus: string;
  viewerReason: string;
}

export type SwapStatus = "PENDING" | "ACCEPTED" | "DECLINED" | "CANCELLED";

export interface SwapRequest {
  id: number;
  fromShiftId: number;
  fromWorkDate: string;
  fromStartTime: string;
  requiredSkill: string;
  toShiftId: number | null;
  toWorkDate: string | null;
  toStartTime: string | null;
  requester: Dm;
  target: Dm;
  status: SwapStatus;
  createdAt: string;
  viewerStatus: string;
  viewerReason: string;
  viewerCanAccept: boolean;
  viewerCanDecline: boolean;
  viewerCanCancel: boolean;
}

export interface ScheduleBoard {
  role: ViewerRole;
  currentDmId: number | null;
  currentDm: Dm | null;
  skills: readonly string[];
  startTimes: readonly string[];
  dms: readonly Dm[];
  shifts: readonly Shift[];
  swaps: readonly SwapRequest[];
}

export interface SchedulingMeta {
  skills: readonly string[];
  startTimes: readonly string[];
  dms: readonly Dm[];
}

export interface ActionResult {
  message: string;
}

export interface CreateShiftPayload {
  workDate: string;
  startTime: string;
  requiredSkill: string;
  requiredCount: number;
}

export interface CreateSwapPayload {
  requesterId: number;
  targetId: number;
  fromShiftId: number;
  toShiftId: number | null;
}
