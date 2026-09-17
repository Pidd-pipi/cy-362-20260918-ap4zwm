import { API_BASE_URL } from "../constants/app";
import { ROLE_MANAGER } from "../constants/scheduling";
import type { ViewerRole } from "../types";
import type {
  ActionResult,
  CreateShiftPayload,
  CreateSwapPayload,
  ScheduleBoard,
  SchedulingMeta,
} from "../types";

export interface IdentityHeaders {
  role: ViewerRole;
  dmId: number | null;
}

function authHeaders(identity: IdentityHeaders): HeadersInit {
  const headers: Record<string, string> = { Accept: "application/json" };
  headers["X-Role"] = identity.role;
  if (identity.role !== ROLE_MANAGER && identity.dmId != null) {
    headers["X-Dm-Id"] = String(identity.dmId);
  }
  return headers;
}

async function parseError(response: Response): Promise<never> {
  let message = `请求失败（${response.status}）`;
  try {
    const body = await response.json();
    if (body && typeof body.message === "string") {
      message = body.message;
    }
  } catch {
    // 非 JSON 错误体时保留兜底文案
  }
  throw new Error(message);
}

export async function fetchSchedulingMeta(identity: IdentityHeaders): Promise<SchedulingMeta> {
  const response = await fetch(`${API_BASE_URL}/scheduling/meta`, {
    headers: authHeaders(identity),
  });
  if (!response.ok) {
    return parseError(response);
  }
  return response.json();
}

export async function fetchScheduleBoard(
  identity: IdentityHeaders,
  options: { swapStatus?: string } = {},
): Promise<ScheduleBoard> {
  const params = new URLSearchParams();
  if (options.swapStatus && options.swapStatus !== "ALL") {
    params.set("swapStatus", options.swapStatus);
  }
  const suffix = params.toString() ? `?${params.toString()}` : "";
  const response = await fetch(`${API_BASE_URL}/scheduling/board${suffix}`, {
    headers: authHeaders(identity),
  });
  if (!response.ok) {
    return parseError(response);
  }
  return response.json();
}

async function postJson<T>(
  path: string,
  identity: IdentityHeaders,
  payload: unknown,
): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "POST",
    headers: {
      ...authHeaders(identity),
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload ?? {}),
  });
  if (!response.ok) {
    return parseError(response);
  }
  return response.json() as Promise<T>;
}

export function createShift(identity: IdentityHeaders, payload: CreateShiftPayload) {
  return postJson<ActionResult>("/scheduling/shifts", identity, payload);
}

export function claimShift(identity: IdentityHeaders, shiftId: number) {
  const body = identity.role === ROLE_MANAGER ? {} : { dmId: identity.dmId };
  return postJson<ActionResult>(`/scheduling/shifts/${shiftId}/claim`, identity, body);
}

export function createSwap(identity: IdentityHeaders, payload: CreateSwapPayload) {
  return postJson<ActionResult>("/scheduling/swaps", identity, payload);
}

export function respondSwap(
  identity: IdentityHeaders,
  swapId: number,
  action: "accept" | "decline" | "cancel",
) {
  const body = identity.role === ROLE_MANAGER ? {} : { dmId: identity.dmId };
  return postJson<ActionResult>(`/scheduling/swaps/${swapId}/${action}`, identity, body);
}
