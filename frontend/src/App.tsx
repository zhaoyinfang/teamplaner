import { useCallback, useEffect, useState } from "react";
import { api, ApiError, tokenStore } from "./api";
import AuthForm from "./AuthForm";
import EventCard from "./EventCard";
import TeamsPanel from "./TeamsPanel";
import type { AttendanceStatus, EventItem, Team, User } from "./types";

export default function App() {
  const [loggedIn, setLoggedIn] = useState(() => tokenStore.get() !== null);
  const [user, setUser] = useState<User | null>(null);
  const [teams, setTeams] = useState<Team[]>([]);
  const [events, setEvents] = useState<EventItem[]>([]);
  const [error, setError] = useState<string | null>(null);

  const logout = useCallback(() => {
    tokenStore.clear();
    setLoggedIn(false);
    setUser(null);
    setTeams([]);
    setEvents([]);
  }, []);

  const load = useCallback(async () => {
    try {
      const [u, t, e] = await Promise.all([api.me(), api.teams(), api.events()]);
      setUser(u);
      setTeams(t);
      setEvents(e);
      setError(null);
    } catch (err) {
      // abgelaufener oder ungueltiger Token -> zurueck zum Login
      if (err instanceof ApiError && err.status === 401) logout();
      else setError(err instanceof Error ? err.message : "Unbekannter Fehler");
    }
  }, [logout]);

  useEffect(() => {
    if (loggedIn) void load();
  }, [loggedIn, load]);

  async function respond(eventId: number, status: AttendanceStatus) {
    try {
      const updated = await api.respond(eventId, status);
      setEvents((list) => list.map((e) => (e.id === updated.id ? updated : e)));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unbekannter Fehler");
    }
  }

  if (!loggedIn) return <AuthForm onLoggedIn={() => setLoggedIn(true)} />;

  return (
    <div className="page">
      <header className="top">
        <h1>Hallo {user?.displayName ?? ""}!</h1>
        <button className="link" onClick={logout}>
          Abmelden
        </button>
      </header>
      {error && <p className="error">{error}</p>}

      <section>
        <h2>Nächste Termine</h2>
        {events.length === 0 && <p className="muted">Keine anstehenden Termine.</p>}
        <div className="events">
          {events.map((e) => (
            <EventCard key={e.id} event={e} onRespond={respond} />
          ))}
        </div>
      </section>

      <TeamsPanel teams={teams} onChanged={load} />
    </div>
  );
}
