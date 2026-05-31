export const BASE_URL = "http://localhost:8080";
const TOKEN_KEY = "jwtToken";
const USER_ID_KEY = "userId";

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
  localStorage.removeItem(TOKEN_KEY);
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
  localStorage.setItem(TOKEN_KEY, token);
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
  if (data.userId != null) {
    saveUserId(data.userId);
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

  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  });

  // token missing/expired → backend returns 401/403 → bounce to login
  if (res.status === 401 || res.status === 403) {
    clearJwtToken();
    window.location.href = "/";
    return Promise.reject(new Error("Session expired"));
  }

  return res;
}
