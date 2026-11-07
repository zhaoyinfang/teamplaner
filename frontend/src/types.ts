export type TeamRole = "OWNER" | "MEMBER";
export type EventType = "TRAINING" | "GAME" | "OTHER";
export type AttendanceStatus = "ACCEPTED" | "DECLINED" | "MAYBE";

export interface TokenResponse {
  accessToken: string;
  tokenType: string;
  expiresInSeconds: number;
}

export interface User {
  id: number;
  email: string;
  displayName: string;
}

export interface Team {
  id: number;
  name: string;
  role: TeamRole;
  inviteCode: string | null; // nur fuer den Owner gesetzt
}

export interface EventItem {
  id: number;
  teamId: number;
  teamName: string;
  title: string;
  type: EventType;
  location: string | null;
  meetAt: string | null;
  startsAt: string;
  endsAt: string | null;
  respondUntil: string | null;
  responseOpen: boolean;
  counts: { accepted: number; maybe: number; declined: number };
  myStatus: AttendanceStatus | null;
}

export interface CreateEventInput {
  title: string;
  type: EventType;
  location?: string;
  startsAt: string;
  endsAt?: string;
  respondUntil?: string;
}
