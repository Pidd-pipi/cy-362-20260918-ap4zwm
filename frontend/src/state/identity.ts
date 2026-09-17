export interface Identity {
  role: "manager" | "dm";
  dmId: number | null;
}

const STORAGE_KEY = "ldmurdergame.identity";

export function loadIdentity(): Identity {
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY);
    if (raw) {
      const parsed = JSON.parse(raw) as Partial<Identity>;
      if (parsed.role === "dm" && typeof parsed.dmId === "number") {
        return { role: "dm", dmId: parsed.dmId };
      }
    }
  } catch {
    // 本地存储不可用时使用默认身份
  }
  return { role: "manager", dmId: null };
}

export function saveIdentity(identity: Identity): void {
  try {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(identity));
  } catch {
    // 忽略持久化失败，不影响页面使用
  }
}
