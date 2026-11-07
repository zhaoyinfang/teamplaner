import { useState, type FormEvent } from "react";
import { api } from "./api";
import type { CreateEventInput, EventType, Team } from "./types";

interface Props {
  teams: Team[];
  onChanged: () => void;
}

function errMsg(e: unknown) {
  return e instanceof Error ? e.message : "Unbekannter Fehler";
}

export default function TeamsPanel({ teams, onChanged }: Props) {
  const [newName, setNewName] = useState("");
  const [code, setCode] = useState("");
  const [error, setError] = useState<string | null>(null);

  async function run(action: () => Promise<unknown>, after?: () => void) {
    setError(null);
    try {
      await action();
      after?.();
      onChanged();
    } catch (e) {
      setError(errMsg(e));
    }
  }

  const create = (e: FormEvent) => {
    e.preventDefault();
    run(() => api.createTeam(newName), () => setNewName(""));
  };
  const join = (e: FormEvent) => {
    e.preventDefault();
    run(() => api.joinTeam(code), () => setCode(""));
  };

  return (
    <section>
      <h2>Meine Teams</h2>
      {error && <p className="error">{error}</p>}

      {teams.length === 0 && <p className="muted">Du bist noch in keinem Team. Erstelle eins oder tritt mit einem Code bei.</p>}

      <ul className="teams">
        {teams.map((t) => (
          <li key={t.id} className="card">
            <div className="row">
              <strong>{t.name}</strong>
              <span className={`badge ${t.role}`}>{t.role === "OWNER" ? "Besitzer" : "Mitglied"}</span>
            </div>
            {t.inviteCode && (
              <p className="muted small">
                Einladungscode: <code>{t.inviteCode}</code>
              </p>
            )}
            {t.role === "OWNER" && <NewEvent teamId={t.id} onCreated={onChanged} />}
          </li>
        ))}
      </ul>

      <div className="grid2">
        <form className="card" onSubmit={create}>
          <h3>Team erstellen</h3>
          <input placeholder="Teamname" value={newName} onChange={(e) => setNewName(e.target.value)} required maxLength={100} />
          <button className="primary">Erstellen</button>
        </form>
        <form className="card" onSubmit={join}>
          <h3>Team beitreten</h3>
          <input placeholder="Einladungscode" value={code} onChange={(e) => setCode(e.target.value)} required maxLength={16} />
          <button className="primary">Beitreten</button>
        </form>
      </div>
    </section>
  );
}

function NewEvent({ teamId, onCreated }: { teamId: number; onCreated: () => void }) {
  const [open, setOpen] = useState(false);
  const [title, setTitle] = useState("Training");
  const [type, setType] = useState<EventType>("TRAINING");
  const [location, setLocation] = useState("");
  const [startsAt, setStartsAt] = useState("");
  const [endsAt, setEndsAt] = useState("");
  const [respondUntil, setRespondUntil] = useState("");
  const [error, setError] = useState<string | null>(null);

  // datetime-local liefert lokale Zeit ohne Zone, das Backend erwartet einen UTC-Zeitpunkt
  const iso = (v: string) => (v ? new Date(v).toISOString() : undefined);

  async function submit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    const input: CreateEventInput = {
      title,
      type,
      location: location || undefined,
      startsAt: iso(startsAt)!,
      endsAt: iso(endsAt),
      respondUntil: iso(respondUntil),
    };
    try {
      await api.createEvent(teamId, input);
      setOpen(false);
      onCreated();
    } catch (err) {
      setError(errMsg(err));
    }
  }

  if (!open) {
    return (
      <button className="link" onClick={() => setOpen(true)}>
        + Termin veröffentlichen
      </button>
    );
  }

  return (
    <form className="eventform" onSubmit={submit}>
      <input value={title} onChange={(e) => setTitle(e.target.value)} placeholder="Titel" required maxLength={150} />
      <select value={type} onChange={(e) => setType(e.target.value as EventType)}>
        <option value="TRAINING">Training</option>
        <option value="GAME">Spiel</option>
        <option value="OTHER">Sonstiges</option>
      </select>
      <input value={location} onChange={(e) => setLocation(e.target.value)} placeholder="Ort (optional)" maxLength={255} />
      <label>
        Beginn
        <input type="datetime-local" value={startsAt} onChange={(e) => setStartsAt(e.target.value)} required />
      </label>
      <label>
        Ende (optional)
        <input type="datetime-local" value={endsAt} onChange={(e) => setEndsAt(e.target.value)} />
      </label>
      <label>
        Antwort bis (optional)
        <input type="datetime-local" value={respondUntil} onChange={(e) => setRespondUntil(e.target.value)} />
      </label>
      {error && <p className="error">{error}</p>}
      <div className="row">
        <button className="primary">Veröffentlichen</button>
        <button type="button" className="link" onClick={() => setOpen(false)}>
          Abbrechen
        </button>
      </div>
    </form>
  );
}
