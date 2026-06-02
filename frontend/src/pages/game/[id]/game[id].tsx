import { useParams, Link } from "react-router-dom";
import { useEffect, useState } from "react";
import { apiFetch, formatUserDateTime } from "../../../api/utils";

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

type CreateAlertRequest = {
  teamName: string;
  alertType: string;
  targetVal: number;
  gameId: number;
};


async function getGameById(id: number): Promise<Game> {
  const res = await apiFetch(`/games/${id}`);
  if (!res.ok) throw new Error("Failed to fetch game");
  return res.json();
}

async function createAlert(request: CreateAlertRequest): Promise<Alert> {
  const res = await apiFetch(`/alerts/add`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(request),
  });
  if (!res.ok) throw new Error("Failed to create alert");
  console.log(res.body)
  return res.json();
}


function GamePage() {
  const { gameId } = useParams<{ gameId: string }>();

  const [game, setGame] = useState<Game | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);


  const [isModalOpen, setIsModalOpen] = useState(false);


  const [formTeamName, setFormTeamName] = useState("");
  const [formAlertType, setFormAlertType] = useState("SCORE_OVER");
  const [formTargetVal, setFormTargetVal] = useState<number>(0);


  const [isSubmitting, setIsSubmitting] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);


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


  function openModal() {
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
    if (!game) return; 

    setIsSubmitting(true);
    setFormError(null);

    try {
      const newAlert = await createAlert({
        teamName: formTeamName,
        alertType: formAlertType,
        targetVal: formTargetVal,
        gameId: Number(gameId),
      });

      setGame(prev => prev ? { ...prev, alerts: [...prev.alerts, newAlert] } : prev);

      closeModal();
    } catch (err) {
      console.error("Failed to create alert:", err);
      setFormError("Could not create alert");
    } finally {
      setIsSubmitting(false);
    }
  }


  if (loading) return <div>Loading game...</div>;
  if (error || !game) return <div>{error || "Game not found"}</div>;

  return (
    <main className="game-detail-page">
      <div className="game-detail-header">
        <h1>Game Details</h1>
          <nav className="games-nav">
            <Link to="/dashboard"> Back to Dashboard </Link>
          </nav>
        <nav className="games-nav">
          <Link to="/games"> Back to Games Page </Link>
        </nav>
        <button className="primary-action" onClick={openModal}>+ Create Alert</button>
      </div>

      <div className="game-score-panel">
        <div className="team-side">
          <span className="team-name">{game.awayTeam}</span>
          <span className="team-score">{game.awayScore}</span>
        </div>
        <div className="match-center">
          <span className={`status-pill status-${game.status.toLowerCase()}`}>{game.status}</span>
          <span className="versus">vs</span>
          <span className="game-time">{formatUserDateTime(game.schedTime)}</span>
        </div>
        <div className="team-side">
          <span className="team-name">{game.homeTeam}</span>
          <span className="team-score">{game.homeScore}</span>
        </div>
      </div>

      <section className="game-alerts-section">
        <h2>Alerts</h2>
        {game.alerts.length === 0 && <p>No alerts yet.</p>}
        {game.alerts.map(a => (
          <div className="game-alert-card" key={a.id}>
            <h3>Alert {a.id}</h3>
            <p>Team: {a.teamName}</p>
            <p>Type: {a.alertType}</p>
            <p>Target: {a.targetVal}</p>
            <p>Status: {a.status}</p>
          </div>
        ))}
      </section>

      {isModalOpen && (
        <div
          className="modal-backdrop"
          onClick={closeModal}
        >
          <div
            className="modal-panel"
            onClick={e => e.stopPropagation()}
          >
            <h2>Create Alert</h2>

            <div className="form-row">
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

            <div className="form-row">
              <label>Alert type:</label>
              <select
                value={formAlertType}
                onChange={e => setFormAlertType(e.target.value)}
              >
                <option value="SCORE_OVER">Score over</option>
                <option value="SCORE_UNDER">Score under</option>
              </select>
            </div>

            <div className="form-row">
              <label>Target value:</label>
              <input
                type="number"
                value={formTargetVal}
                onChange={e => setFormTargetVal(Number(e.target.value))}
              />
            </div>

            {formError && <p className="form-error">{formError}</p>}

            <div className="modal-actions">
              <button onClick={closeModal} disabled={isSubmitting}>Cancel</button>
              <button className="primary-action" onClick={handleSubmit} disabled={isSubmitting || !formTeamName}>
                {isSubmitting ? "Creating..." : "Create"}
              </button>
            </div>
          </div>
        </div>
      )}
    </main>
  );
}

export default GamePage;
