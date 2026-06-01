import { useState, useEffect} from 'react';
import { Link } from 'react-router-dom';
import { apiFetch, formatUserDateTime } from '../../api/utils';
type AlertType = 'SCORE_OVER' | 'SCORE_UNDER';
type AlertStatus = 'CREATED' | 'TRIGGERED' | "FINISHED";
type GameStatus = 'SCHEDULED' | 'LIVE' | 'FINAL';

export interface AlertDto {
  id: number;
  teamName: string;
  alertType: AlertType;
  targetVal: number;
  status: AlertStatus;
  createdAt: string;
  triggeredAt?: string;
  gameId?: number;
  actualGameId?: string;
  homeTeam?: string;
  awayTeam?: string;
  homeScore?: number;
  awayScore?: number;
  gameStatus?: GameStatus;
}
type FilterStatus = AlertStatus | 'ALL';
async function getAlerts() : Promise<AlertDto[]>
{
    const res = await apiFetch("/alerts");
    if (!res.ok) {
        throw new Error("Failed to fetch alerts");
    }
    const alerts : AlertDto[] = await res.json();
    return alerts;
}

function AlertsList() {
  const [alerts, setAlerts] = useState<AlertDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [statusFilter, setStatusFilter] = useState<FilterStatus>('ALL');

  useEffect(() => {
    async function loadAlerts() {
    try {
        const data = await getAlerts();
        setAlerts(data);
      }
      catch (err)
      {
        setError("Could not load alerts");
      }
      finally {
        setLoading(false);
      } 
    }
    loadAlerts();
    }, []);

  const visibleAlerts = statusFilter === 'ALL' ? alerts : alerts.filter(a => a.status === statusFilter);

  if (loading) return <div className="alerts-page">Loading alerts...</div>;
  if(error) return <div className="alerts-page">{error || "Alerts failed to load..."}</div>;

  return (
    <main className="alerts-page">
      <div className="alerts-header">
        <h1>Alerts</h1>
        <nav className="alerts-nav">
          <Link to="/dashboard">Back to Dashboard</Link>
        </nav>
      </div>

      <div className="alerts-filter-bar">
        <button className={statusFilter === "ALL" ? "is-active" : ""} onClick={() => setStatusFilter("ALL")}>
          All
        </button>
        <button className={statusFilter === "CREATED" ? "is-active" : ""} onClick={() => setStatusFilter("CREATED")}>
          Active
        </button>
        <button className={statusFilter === "TRIGGERED" ? "is-active" : ""} onClick={() => setStatusFilter("TRIGGERED")}>
          Triggered
        </button>
        <button className={statusFilter === "FINISHED" ? "is-active" : ""} onClick={() => setStatusFilter("FINISHED")}>
          Finished
        </button>
      </div>

      {visibleAlerts.length === 0 ? (
        <p className="empty-state">No alerts found.</p>
      ) : (
        <ul className="alerts-list">
        {visibleAlerts.map(alert => (
          <li className="alert-list-card" key={alert.id}>
            <div className="alert-list-main">
              <span className="alert-team">{alert.teamName}</span>
              <span>{alert.alertType.replace("_", " ")}</span>
              <strong>{alert.targetVal}</strong>
            </div>
            <div className="alert-list-meta">
              <span className={`alert-status-pill alert-status-${alert.status.toLowerCase()}`}>{alert.status}</span>
              <span>Created {formatUserDateTime(alert.createdAt)}</span>
              {alert.triggeredAt && <span>Triggered {formatUserDateTime(alert.triggeredAt)}</span>}
            </div>
            {(alert.awayTeam || alert.homeTeam) && (
              <div className="alert-game-context">
                <span>{alert.awayTeam ?? "Away"} {alert.awayScore ?? "-"}</span>
                <span>vs</span>
                <span>{alert.homeTeam ?? "Home"} {alert.homeScore ?? "-"}</span>
              </div>
            )}
          </li>
        ))}
        </ul>
      )}
    </main>
  );
}
export default AlertsList
