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

export interface DmProfile {
  id: number;
  name: string;
  skills: string[];
}

export interface ShiftAssignmentView {
  dmId: number;
  dmName: string;
}

export interface ShiftViewerState {
  state: "CLAIMABLE" | "CLAIMED" | "FULL" | "SKILL_MISMATCH" | "SLOT_CONFLICT";
  reason: string;
}

export interface ShiftGapView {
  id: number;
  shiftDate: string;
  slot: string;
  skill: string;
  requiredCount: number;
  assignedCount: number;
  gap: number;
  full: boolean;
  assignments: ShiftAssignmentView[];
  viewer: ShiftViewerState | null;
}

export interface MyShiftView {
  assignmentId: number;
  shiftId: number;
  shiftDate: string;
  slot: string;
  skill: string;
  requiredCount: number;
  assignedCount: number;
}

export type SwapStatus = "OPEN" | "ACCEPTED" | "COMPLETED" | "CANCELLED";

export interface SwapRequestView {
  id: number;
  shiftId: number;
  shiftDate: string;
  slot: string;
  skill: string;
  fromDmId: number;
  fromDmName: string;
  toDmId: number | null;
  toDmName: string | null;
  status: SwapStatus;
  note: string | null;
  eligible: boolean;
  reason: string | null;
  createdAt: string;
}

export interface ScheduleBoard {
  date: string;
  slots: string[];
  skills: string[];
  dms: DmProfile[];
  shifts: ShiftGapView[];
  myShifts: MyShiftView[];
  swaps: SwapRequestView[];
}
