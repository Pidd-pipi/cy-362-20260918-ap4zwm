import { computed, reactive, readonly } from "vue";
import {
  IDENTITY_DM_KEY,
  IDENTITY_ROLE_KEY,
  ROLE_DM,
  ROLE_MANAGER,
} from "../constants/scheduling";
import type { ViewerRole } from "../types";
import {
  claimShift as apiClaimShift,
  createShift as apiCreateShift,
  createSwap as apiCreateSwap,
  fetchScheduleBoard,
  respondSwap as apiRespondSwap,
  type IdentityHeaders,
} from "../api/scheduling";
import type {
  CreateShiftPayload,
  CreateSwapPayload,
  Dm,
  ScheduleBoard,
  Shift,
} from "../types";

interface SchedulingState {
  role: ViewerRole;
  dmId: number | null;
  swapFilter: string;
  board: ScheduleBoard | null;
  loading: boolean;
  error: string;
  lastLoadedAt: string;
  online: boolean;
}

function readStoredRole(): ViewerRole {
  return localStorage.getItem(IDENTITY_ROLE_KEY) === ROLE_MANAGER ? ROLE_MANAGER : ROLE_DM;
}

function readStoredDmId(): number | null {
  const raw = localStorage.getItem(IDENTITY_DM_KEY);
  if (!raw) {
    return null;
  }
  const value = Number(raw);
  return Number.isFinite(value) ? value : null;
}

const state = reactive<SchedulingState>({
  role: readStoredRole(),
  dmId: readStoredDmId(),
  swapFilter: "PENDING",
  board: null,
  loading: false,
  error: "",
  lastLoadedAt: "",
  online: false,
});

function identity(): IdentityHeaders {
  return { role: state.role, dmId: state.role === ROLE_MANAGER ? null : state.dmId };
}

async function refreshBoard(options: { silent?: boolean } = {}): Promise<void> {
  if (!options.silent) {
    state.loading = true;
  }
  try {
    state.board = await fetchScheduleBoard(identity(), { swapStatus: state.swapFilter });
    // 服务端以数据中的身份为准，首次选择 DM 后同步本地存储
    if (state.role === ROLE_DM && state.board.currentDmId != null) {
      state.dmId = state.board.currentDmId;
      localStorage.setItem(IDENTITY_DM_KEY, String(state.dmId));
    }
    state.error = "";
    state.online = true;
    state.lastLoadedAt = new Date().toLocaleTimeString("zh-CN", { hour12: false });
  } catch (error) {
    state.online = false;
    state.error = error instanceof Error ? error.message : "排班数据加载失败";
  } finally {
    state.loading = false;
  }
}

async function setRole(role: ViewerRole): Promise<void> {
  state.role = role;
  localStorage.setItem(IDENTITY_ROLE_KEY, role);
  await refreshBoard();
}

async function setDm(dmId: number): Promise<void> {
  state.dmId = dmId;
  localStorage.setItem(IDENTITY_DM_KEY, String(dmId));
  await refreshBoard();
}

async function setSwapFilter(filter: string): Promise<void> {
  state.swapFilter = filter;
  await refreshBoard();
}

async function runAction(action: () => Promise<{ message: string }>): Promise<string> {
  const result = await action();
  await refreshBoard({ silent: true });
  return result.message;
}

function addShift(payload: CreateShiftPayload) {
  return runAction(() => apiCreateShift(identity(), payload));
}

function claim(shiftId: number) {
  return runAction(() => apiClaimShift(identity(), shiftId));
}

function requestSwap(payload: CreateSwapPayload) {
  return runAction(() => apiCreateSwap(identity(), payload));
}

function swapAction(swapId: number, action: "accept" | "decline" | "cancel") {
  return runAction(() => apiRespondSwap(identity(), swapId, action));
}

// 按日期升序聚合
const shiftsByDate = computed<Array<{ date: string; shifts: readonly Shift[] }>>(() => {
  if (!state.board) {
    return [];
  }
  const groups = new Map<string, Shift[]>();
  for (const shift of state.board.shifts) {
    const list = groups.get(shift.workDate) ?? [];
    list.push(shift);
    groups.set(shift.workDate, list);
  }
  return [...groups.entries()]
    .sort((a, b) => a[0].localeCompare(b[0]))
    .map(([date, shifts]) => ({ date, shifts }));
});

const myShifts = computed<readonly Shift[]>(() => {
  if (!state.board || state.role === ROLE_MANAGER) {
    return [];
  }
  return state.board.shifts
    .filter((shift) => shift.viewerStatus === "CLAIMED_BY_ME")
    .sort((a, b) =>
      a.workDate === b.workDate
        ? a.startTime.localeCompare(b.startTime)
        : a.workDate.localeCompare(b.workDate));
});

/** 班次缺口：缺多少人 + 不可认领原因都直接来自服务端。 */
const gapCount = computed(() => {
  if (!state.board) {
    return 0;
  }
  return state.board.shifts.reduce((sum, shift) => {
    return sum + Math.max(0, shift.requiredCount - shift.assignedCount);
  }, 0);
});

const dms = computed<readonly Dm[]>(() => state.board?.dms ?? []);
const skills = computed<readonly string[]>(() => state.board?.skills ?? []);
const startTimes = computed<readonly string[]>(() => state.board?.startTimes ?? ["14:00", "19:00"]);

const pendingSwaps = computed(() =>
  (state.board?.swaps ?? []).filter((swap) => swap.status === "PENDING"));

let poller: ReturnType<typeof setInterval> | null = null;
function startPolling(): void {
  if (poller) {
    return;
  }
  poller = setInterval(() => {
    void refreshBoard({ silent: true });
  }, 30_000);
}

function init(): void {
  startPolling();
  void refreshBoard();
}

export function useScheduling() {
  return {
    state: readonly(state),
    shiftsByDate,
    myShifts,
    gapCount,
    pendingSwaps,
    dms,
    skills,
    startTimes,
    init,
    refreshBoard,
    setRole,
    setDm,
    setSwapFilter,
    addShift,
    claim,
    requestSwap,
    swapAction,
  };
}
