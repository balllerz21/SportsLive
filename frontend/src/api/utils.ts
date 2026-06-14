export const BASE_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";
const TOKEN_KEY = "jwtToken";
const USER_ID_KEY = "id";
const GET_CACHE_TTL_MS = 30_000;

type CachedResponse = {
  expiresAt: number;
  response: Response;
};

const getCache = new Map<string, CachedResponse>();
const inFlightGets = new Map<string, Promise<Response>>();

function clearApiCache() {
  getCache.clear();
  inFlightGets.clear();
}

function decodeJwtPayload(token: string): { exp?: number } {
  const payload = token.split(".")[1];
  if (!payload) {
    throw new Error("Invalid JWT");
  }

  const base64 = payload.replace(/-/g, "+").replace(/_/g, "/");
  const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), "=");
  return JSON.parse(atob(padded));
}

export function clearJwtToken() {
  clearApiCache();
  localStorage.removeItem(TOKEN_KEY);
  clearUserId();
}

export function isJwtExpired(token: string) {
  try {
    const payload = decodeJwtPayload(token);
    if (!payload.exp) {
      return false;
    }

    return payload.exp * 1000 <= Date.now();
  } catch {
    return true;
  }
}

export function getJwtToken() {
  const token = localStorage.getItem(TOKEN_KEY);
  if (!token || token === "undefined" || token === "null" || isJwtExpired(token)) {
    clearJwtToken();
    return null;
  }

  return token;
}

export function saveJwtToken(token: string) {
  clearApiCache();
  localStorage.setItem(TOKEN_KEY, token);
}

export function warmBackend() {
  void fetch(`${BASE_URL}/actuator/health`, {
    cache: "no-store",
    headers: { Accept: "application/json" },
  }).catch(() => undefined);
}

export async function loginUser(username: string, password: string) {
  clearJwtToken();
  const res = await fetch(`${BASE_URL}/users/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, passwordHash: password }),
  });

  if (!res.ok) {
    throw new Error(await getResponseErrorMessage(res));
  }

  const data = await res.json();
  const token = data.accessToken ?? data.token;

  if (!token || isJwtExpired(token)) {
    throw new Error("Login did not return a valid token");
  }

  saveJwtToken(token);
  if (data.id != null) {
    saveUserId(data.id);
  }
  return data;
}

export function saveUserId(userId: number | string) {
  localStorage.setItem(USER_ID_KEY, String(userId));
}
export function getUserId(): string | null {
  return localStorage.getItem(USER_ID_KEY);
}
export function clearUserId() {
  localStorage.removeItem(USER_ID_KEY);
}

export function formatUserDateTime(value: string | number | Date | null | undefined) {
  if (!value) {
    return "TBD";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "TBD";
  }

  return new Intl.DateTimeFormat(undefined, {
    month: "short",
    day: "numeric",
    year: "numeric",
    hour: "numeric",
    minute: "2-digit",
    timeZoneName: "short",
  }).format(date);
}

export async function getResponseErrorMessage(res: Response) {
  const text = await res.text();
  if (!text) {
    return `Request failed with status ${res.status}`;
  }

  try {
    const data = JSON.parse(text);
    return data.message || data.error || text;
  } catch {
    return text;
  }
}

export async function apiFetch(path: string, options: RequestInit = {}) {
  const token = getJwtToken();
  const method = (options.method ?? "GET").toUpperCase();
  const cacheKey = `${token ?? "anonymous"}:${path}`;

  if (method === "GET") {
    const cached = getCache.get(cacheKey);
    if (cached && cached.expiresAt > Date.now()) {
      return cached.response.clone();
    }
    getCache.delete(cacheKey);
  } else {
    clearApiCache();
  }

  const performFetch = async () => {
    const res = await fetch(`${BASE_URL}${path}`, {
      ...options,
      method,
      headers: {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.headers,
      },
    });

    if (res.status === 401 || res.status === 403) {
      clearJwtToken();
      window.location.href = "/";
      throw new Error("Session expired");
    }

    if (method === "GET" && res.ok) {
      getCache.set(cacheKey, {
        expiresAt: Date.now() + GET_CACHE_TTL_MS,
        response: res.clone(),
      });
    }

    return res;
  };

  if (method !== "GET") {
    return performFetch();
  }

  let request = inFlightGets.get(cacheKey);
  if (!request) {
    request = performFetch().finally(() => inFlightGets.delete(cacheKey));
    inFlightGets.set(cacheKey, request);
  }

  return (await request).clone();
}

export function prefetchApi(path: string) {
  void apiFetch(path)
    .then(response => response.body?.cancel())
    .catch(() => undefined);
}
