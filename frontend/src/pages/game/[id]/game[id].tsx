import { useParams } from "react-router-dom";
import { useEffect, useState } from "react";

// ============================================================
// TYPES — same shape as your backend
// ============================================================
type Alert = {
  id: number;
  teamName: string;
  alertType: string;
  targetVal: number;
  status: string;
};

type Game = {
  id: number;
  homeTeam: string;
  homeScore: number;
  awayTeam: string;
  awayScore: number;
  schedTime: string;  
  status: string;
  alerts: Alert[];
};

// What we SEND when creating a new alert.
// No id (backend generates), no status (backend sets default), adds gameId.
type CreateAlertRequest = {
  teamName: string;
  alertType: string;
  targetVal: number;
  gameId: number;
};

// ============================================================
// API FUNCTIONS — talk to the backend
// ============================================================

async function getGameById(id: number): Promise<Game> {
  const res = await fetch(`http://localhost:8080/games/${id}`);
  if (!res.ok) throw new Error("Failed to fetch game");
  return res.json();
}

// NEW: creates an alert. Returns the created alert (backend sends it back with id + status).
async function createAlert(request: CreateAlertRequest): Promise<Alert> {
  const res = await fetch(`http://localhost:8080/alerts/add`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(request),
  });
  if (!res.ok) throw new Error("Failed to create alert");
  console.log(res.body)
  return res.json();
}

// ============================================================
// COMPONENT
// ============================================================

function GamePage() {
  const { gameId } = useParams<{ gameId: string }>();

  // ---- Game state ----
  const [game, setGame] = useState<Game | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // ---- Modal / form state ----
  // Controls whether the modal is open or closed.
  const [isModalOpen, setIsModalOpen] = useState(false);

  // Each form field has its own state. These are "controlled inputs":
  // the input's value comes FROM state, and typing UPDATES state.
  const [formTeamName, setFormTeamName] = useState("");
  const [formAlertType, setFormAlertType] = useState("SCORE_OVER");
  const [formTargetVal, setFormTargetVal] = useState<number>(0);

  // Submitting / error state for the form itself (separate from the page error).
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  // ---- Fetch the game on mount ----
  useEffect(() => {
    async function loadGame() {
      try {
        if (!gameId) return;
        const data = await getGameById(Number(gameId));
        setGame(data);
      } catch (err) {
        console.error("Failed to load game:", err);
        setError("Could not load game");
      } finally {
        setLoading(false);
      }
    }
    loadGame();
  }, [gameId]);

  // ---- Handlers ----

  function openModal() {
    // Reset the form every time we open it, so stale values don't linger.
    setFormTeamName("");
    setFormAlertType("SCORE_OVER");
    setFormTargetVal(0);
    setFormError(null);
    setIsModalOpen(true);
  }

  function closeModal() {
    setIsModalOpen(false);
  }

  async function handleSubmit() {
    if (!game) return; // safety: shouldn't happen, but TypeScript needs the check

    setIsSubmitting(true);
    setFormError(null);

    try {
      const newAlert = await createAlert({
        teamName: formTeamName,
        alertType: formAlertType,
        targetVal: formTargetVal,
        gameId: Number(gameId),
      });

      // Optimistically add the new alert to the page without re-fetching the whole game.
      // This uses the "functional" setState form because we're deriving new state from old state.
      setGame(prev => prev ? { ...prev, alerts: [...prev.alerts, newAlert] } : prev);

      closeModal();
    } catch (err) {
      console.error("Failed to create alert:", err);
      setFormError("Could not create alert");
    } finally {
      setIsSubmitting(false);
    }
  }

  // ---- Render ----

  if (loading) return <div>Loading game...</div>;
  if (error || !game) return <div>{error || "Game not found"}</div>;

  return (
    <div>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <h1>Game Details</h1>
        <button onClick={openModal}>+ Create Alert</button>
      </div>

      <div>
        <p>
          {game.awayTeam} {game.awayScore} - {game.homeTeam} {game.homeScore}
        </p>
        <p>Status: {game.status}</p>
        <p>Time: {game.schedTime}</p>
      </div>

      <div>
        <h2>Alerts</h2>
        {game.alerts.length === 0 && <p>No alerts yet.</p>}
        {game.alerts.map(a => (
          <div key={a.id} style={{ border: "1px solid #ccc", padding: 8, margin: "8px 0" }}>
            <h3>Alert {a.id}</h3>
            <p>Team: {a.teamName}</p>
            <p>Type: {a.alertType}</p>
            <p>Target: {a.targetVal}</p>
            <p>Status: {a.status}</p>
          </div>
        ))}
      </div>

      {/* ====== MODAL ====== */}
      {isModalOpen && (
        <div
          style={{
            position: "fixed", top: 0, left: 0, right: 0, bottom: 0,
            background: "rgba(0,0,0,0.5)",
            display: "flex", alignItems: "center", justifyContent: "center",
            zIndex: 100,
          }}
          onClick={closeModal}
        >
          <div
            style={{ background: "white", padding: 24, borderRadius: 8, minWidth: 320 }}
            onClick={e => e.stopPropagation()}
          >
            <h2>Create Alert</h2>

            <div style={{ marginBottom: 12 }}>
              <label>Team:</label>
              <select
                value={formTeamName}
                onChange={e => setFormTeamName(e.target.value)}
              >
                <option value="">-- pick a team --</option>
                <option value={game.homeTeam}>{game.homeTeam}</option>
                <option value={game.awayTeam}>{game.awayTeam}</option>
              </select>
            </div>

            <div style={{ marginBottom: 12 }}>
              <label>Alert type:</label>
              <select
                value={formAlertType}
                onChange={e => setFormAlertType(e.target.value)}
              >
                <option value="SCORE_OVER">Score over</option>
                <option value="SCORE_BELOW">Score below</option>
              </select>
            </div>

            <div style={{ marginBottom: 12 }}>
              <label>Target value:</label>
              <input
                type="number"
                value={formTargetVal}
                onChange={e => setFormTargetVal(Number(e.target.value))}
              />
            </div>

            {/* {formError && <p style={{ color: "red" }}>{formError}</p>} */}

            <div style={{ display: "flex", gap: 8, justifyContent: "flex-end" }}>
              <button onClick={closeModal} disabled={isSubmitting}>Cancel</button>
              <button onClick={handleSubmit} disabled={isSubmitting || !formTeamName}>
                {isSubmitting ? "Creating..." : "Create"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default GamePage;