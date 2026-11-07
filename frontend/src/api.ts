import type {
  AttendanceStatus,
  CreateEventInput,
  EventItem,
  Team,
  TokenResponse,
  User,
} from "./types";

const TOKEN_KEY = "teamplaner.token";

export const tokenStore = {
  get: (): string | null => localStorage.getItem(TOKEN_KEY),
  set: (t: string) => localStorage.setItem(TOKEN_KEY, t),
  clear: () => localStorage.removeItem(TOKEN_KEY),
};

export class ApiError extends Error {
  status: number;
  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers: Record<string, string> = { "Content-Type": "application/json" };
  const token = tokenStore.get();
  if (token) headers.Authorization = `Bearer ${token}`;

  const res = await fetch(path, { ...init, headers: { ...headers, ...(init.headers as object) } });

  if (!res.ok) {
    // Das Backend liefert Fehler als RFC-7807-ProblemDetail (Feld "detail")
    let message = `Fehler ${res.status}`;
    try {
      const body = await res.json();
      if (body.errors) message = Object.values(body.errors as Record<string, string>).join(", ");
      else if (body.detail) message = body.detail;
    } catch {
      /* Antwort ohne JSON-Body */
    }
    throw new ApiError(res.status, message);
  }
  return res.status === 204 ? (undefined as T) : ((await res.json()) as T);
}

const json = (body: unknown): RequestInit => ({ method: "POST", body: JSON.stringify(body) });

export const api = {
  register: (email: string, password: string, displayName: string) =>
    request<TokenResponse>("/api/auth/register", json({ email, password, displayName })),
  login: (email: string, password: string) =>
    request<TokenResponse>("/api/auth/login", json({ email, password })),
  me: () => request<User>("/api/users/me"),

  teams: () => request<Team[]>("/api/teams"),
  createTeam: (name: string) => request<Team>("/api/teams", json({ name })),
  joinTeam: (inviteCode: string) => request<Team>("/api/teams/join", json({ inviteCode })),

  events: () => request<EventItem[]>("/api/events"),
  createEvent: (teamId: number, input: CreateEventInput) =>
    request<EventItem>(`/api/teams/${teamId}/events`, json(input)),
  respond: (eventId: number, status: AttendanceStatus) =>
    request<EventItem>(`/api/events/${eventId}/attendance`, {
      method: "PUT",
      body: JSON.stringify({ status }),
    }),
};
