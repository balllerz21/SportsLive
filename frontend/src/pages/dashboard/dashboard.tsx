import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { apiFetch, formatUserDateTime, getResponseErrorMessage, prefetchApi } from "../../api/utils";

type GameStatus = "SCHEDULED" | "LIVE" | "FINAL";

type Game = {
  id: number;
  homeTeam: string;
  homeScore: number;
  awayTeam: string;
  awayScore: number;
  schedTime: string;
  status: GameStatus;
};

type AlertStatus = "CREATED" | "TRIGGERED" | "FINISHED";

type AlertDto = {
  id: number;
  teamName: string;
  alertType: string;
  targetVal: number;
  status: AlertStatus;
  gameId?: number;
  homeTeam?: string;
  awayTeam?: string;
};

async function getLiveGames() {
  const res = await apiFetch("/games?status=LIVE&size=20&sort=updatedTime,desc");
  if (!res.ok) {
    throw new Error(await getResponseErrorMessage(res));
  }

  return res.json() as Promise<Game[]>;
}

async function getActiveAlerts() {
  const res = await apiFetch("/alerts?status=CREATED");
  if (!res.ok) {
    throw new Error(await getResponseErrorMessage(res));
  }

  return res.json() as Promise<AlertDto[]>;
}

function DashboardPage() {
  const [liveGames, setLiveGames] = useState<Game[]>([]);
  const [activeAlerts, setActiveAlerts] = useState<AlertDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  async function refreshDashboard() {
    const [games, alerts] = await Promise.all([
      getLiveGames(),
      getActiveAlerts(),
    ]);

    setLiveGames(games);
    setActiveAlerts(alerts);
  }

  useEffect(() => {
    async function loadDashboard() {
      try {
        await refreshDashboard();
        prefetchApi("/games?page=0&size=50&sort=updatedTime,desc");
      } catch (err) {
        setError(err instanceof Error ? err.message : "Could not load dashboard");
      } finally {
        setLoading(false);
      }
    }

    loadDashboard();
  }, []);

  useEffect(() => {
    const intervalId = window.setInterval(async () => {
      try {
        await refreshDashboard();
      } catch (err) {
        console.error("Dashboard polling failed", err);
      }
    }, 60000);

    return () => window.clearInterval(intervalId);
  }, []);

  if (loading) return <div className="dashboard-page">Loading dashboard...</div>;
  if (error) return <div className="dashboard-page">{error}</div>;

  return (
    <main className="dashboard-page">
      <div className="dashboard-header">
        <h1>Dashboard</h1>
        <nav className="dashboard-nav">
          <Link to="/games">Games</Link>
          <Link to="/alerts">Alerts</Link>
        </nav>
      </div>

      <section className="live-games-section">
        <h2>Live Games</h2>
        {liveGames.length === 0 ? (
          <p className="empty-state">No live games at the moment.</p>
        ) : (
          <div className="live-games-list">
            {liveGames.map(game => (
              <Link className="live-game-panel" to={`/games/${game.id}`} key={game.id}>
                <div className="team-side">
                  <span className="team-name">{game.awayTeam}</span>
                  <span className="team-score">{game.awayScore}</span>
                </div>
                <div className="match-center">
                  <span className="live-pill">LIVE</span>
                  <span className="versus">vs</span>
                  <span className="game-time">{formatUserDateTime(game.schedTime)}</span>
                </div>
                <div className="team-side">
                  <span className="team-name">{game.homeTeam}</span>
                  <span className="team-score">{game.homeScore}</span>
                </div>
              </Link>
            ))}
          </div>
        )}
      </section>

      <section className="active-alerts-section">
        <h2>Active Alerts</h2>
        {activeAlerts.length === 0 ? (
          <p className="empty-state">No active alerts right now.</p>
        ) : (
          <div className="active-alert-list">
            {activeAlerts.map(alert => (
              <Link
                className="active-alert-row"
                to={alert.gameId ? `/games/${alert.gameId}` : "/alerts"}
                key={alert.id}
              >
                <span>{alert.teamName}</span>
                <span>{alert.alertType.replace("_", " ")}</span>
                <strong>{alert.targetVal}</strong>
              </Link>
            ))}
          </div>
        )}
      </section>
    </main>
  );
}

export default DashboardPage;
