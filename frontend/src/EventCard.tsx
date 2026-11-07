import type { AttendanceStatus, EventItem } from "./types";

const TYPE_LABEL = { TRAINING: "Training", GAME: "Spiel", OTHER: "Termin" } as const;

const fmt = (iso: string) =>
  new Date(iso).toLocaleString("de-DE", {
    weekday: "short",
    day: "2-digit",
    month: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });

interface Props {
  event: EventItem;
  onRespond: (eventId: number, status: AttendanceStatus) => void;
}

export default function EventCard({ event, onRespond }: Props) {
  const btn = (status: AttendanceStatus, label: string, count: number) => (
    <button
      className={`resp ${event.myStatus === status ? "active " + status : ""}`}
      disabled={!event.responseOpen}
      onClick={() => onRespond(event.id, status)}
    >
      {label} <strong>{count}</strong>
    </button>
  );

  return (
    <article className={`event ${event.type}`}>
      <header>
        <span className="type">{TYPE_LABEL[event.type]}</span>
        <h3>{event.title}</h3>
        <span className="muted">{event.teamName}</span>
      </header>
      <div className="times">
        <div>
          <span className="muted">Beginn</span>
          <br />
          {fmt(event.startsAt)}
        </div>
        {event.meetAt && (
          <div>
            <span className="muted">Treffen</span>
            <br />
            {fmt(event.meetAt)}
          </div>
        )}
        {event.location && (
          <div>
            <span className="muted">Ort</span>
            <br />
            {event.location}
          </div>
        )}
      </div>
      {!event.responseOpen && <p className="error small">Zeit zum Zu-/Absagen abgelaufen</p>}
      <footer>
        {btn("ACCEPTED", "Zusagen", event.counts.accepted)}
        {btn("MAYBE", "Vielleicht", event.counts.maybe)}
        {btn("DECLINED", "Absagen", event.counts.declined)}
      </footer>
    </article>
  );
}
