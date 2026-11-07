import { useState, type FormEvent } from "react";
import { api, tokenStore } from "./api";

export default function AuthForm({ onLoggedIn }: { onLoggedIn: () => void }) {
  const [mode, setMode] = useState<"login" | "register">("login");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [displayName, setDisplayName] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function submit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setBusy(true);
    try {
      const res =
        mode === "login"
          ? await api.login(email, password)
          : await api.register(email, password, displayName);
      tokenStore.set(res.accessToken);
      onLoggedIn();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unbekannter Fehler");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="auth">
      <h1>Teamplaner</h1>
      <p className="muted">Termine, Zu- und Absagen für dein Team.</p>
      <form className="card" onSubmit={submit}>
        <h2>{mode === "login" ? "Anmelden" : "Registrieren"}</h2>
        {mode === "register" && (
          <label>
            Anzeigename
            <input value={displayName} onChange={(e) => setDisplayName(e.target.value)} required maxLength={100} />
          </label>
        )}
        <label>
          E-Mail
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </label>
        <label>
          Passwort {mode === "register" && <span className="muted">(mind. 8 Zeichen)</span>}
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            minLength={mode === "register" ? 8 : undefined}
          />
        </label>
        {error && <p className="error">{error}</p>}
        <button className="primary" disabled={busy}>
          {mode === "login" ? "Anmelden" : "Konto erstellen"}
        </button>
        <button type="button" className="link" onClick={() => setMode(mode === "login" ? "register" : "login")}>
          {mode === "login" ? "Noch kein Konto? Registrieren" : "Schon ein Konto? Anmelden"}
        </button>
      </form>
    </div>
  );
}
