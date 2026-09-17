import { API_BASE_URL } from "../constants/app";
import type { ScheduleBoard } from "../types";

interface ApiMessage {
  message: string;
}

async function parseError(response: Response): Promise<never> {
  let message = `请求失败（${response.status}）`;
  try {
    const data = (await response.json()) as Partial<ApiMessage>;
    if (typeof data.message === "string" && data.message.length > 0) {
      message = data.message;
    }
  } catch {
    // 保留默认提示
  }
  throw new Error(message);
}

async function post<T>(path: string, body: unknown): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "POST",
    headers: { "Content-Type": "application/json", Accept: "application/json" },
    body: JSON.stringify(body),
  });
  if (!response.ok) {
    await parseError(response);
  }
  return response.json() as Promise<T>;
}

export async function fetchScheduleBoard(date: string, dmId: number | null): Promise<ScheduleBoard> {
  const params = new URLSearchParams({ date });
  if (dmId !== null) {
    params.set("dmId", String(dmId));
  }
  const response = await fetch(`${API_BASE_URL}/schedule/board?${params.toString()}`, {
    headers: { Accept: "application/json" },
  });
  if (!response.ok) {
    await parseError(response);
  }
  return response.json() as Promise<ScheduleBoard>;
}

export interface CreateShiftPayload {
  shiftDate: string;
  slot: string;
  skill: string;
  requiredCount: number;
  createdBy: string;
}

export function createShift(payload: CreateShiftPayload) {
  return post<ApiMessage>("/schedule/shifts", payload);
}

export function claimShift(shiftId: number, dmId: number) {
  return post<ApiMessage>(`/schedule/shifts/${shiftId}/claim`, { dmId });
}

export function createSwapRequest(shiftId: number, dmId: number, note: string) {
  return post<ApiMessage>("/schedule/swaps", { shiftId, dmId, note });
}

export function acceptSwap(swapId: number, dmId: number) {
  return post<ApiMessage>(`/schedule/swaps/${swapId}/accept`, { dmId });
}

export function confirmSwap(swapId: number, dmId: number) {
  return post<ApiMessage>(`/schedule/swaps/${swapId}/confirm`, { dmId });
}

export function cancelSwap(swapId: number, dmId: number) {
  return post<ApiMessage>(`/schedule/swaps/${swapId}/cancel`, { dmId });
}
