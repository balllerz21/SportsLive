import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { apiFetch, formatUserDateTime, getResponseErrorMessage } from '../../api/utils';
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
type FilterAlertType = AlertType | 'ALL';
type FilterPeriod = 'ALL' | 'daily' | 'weekly' | 'monthly' | 'yearly';

type AlertFilters = {
  status: FilterStatus;
  alertType: FilterAlertType;
  teamName: string;
  period: FilterPeriod;
};

async function getAlerts(filters: AlertFilters) : Promise<AlertDto[]>
{
    const params = new URLSearchParams();
    if (filters.status !== 'ALL') params.set('status', filters.status);
    if (filters.alertType !== 'ALL') params.set('alertType', filters.alertType);
    if (filters.teamName.trim()) params.set('teamName', filters.teamName.trim());
    if (filters.period !== 'ALL') params.set('period', filters.period);

    const query = params.toString();
    const res = await apiFetch(`/alerts${query ? `?${query}` : ""}`);
    if (!res.ok) {
        throw new Error(await getResponseErrorMessage(res));
    }
    const alerts : AlertDto[] = await res.json();
    return alerts;
}

function AlertsList() {
  const [alerts, setAlerts] = useState<AlertDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [statusFilter, setStatusFilter] = useState<FilterStatus>('ALL');
  const [typeFilter, setTypeFilter] = useState<FilterAlertType>('ALL');
  const [teamFilter, setTeamFilter] = useState('');
  const [periodFilter, setPeriodFilter] = useState<FilterPeriod>('ALL');

  const refreshAlerts = useCallback(async () => {
    const data = await getAlerts({
      status: statusFilter,
      alertType: typeFilter,
      teamName: teamFilter,
      period: periodFilter,
    });
    setAlerts(data);
  }, [statusFilter, typeFilter, teamFilter, periodFilter]);

  useEffect(() => {
    const timeoutId = window.setTimeout(() => {
    async function loadAlerts() {
    try {
        setError(null);
        await refreshAlerts();
      }
      catch (err)
      {
        const message = err instanceof Error ? err.message : "Could not load alerts";
        setError(message);
      }
      finally {
        setLoading(false);
      } 
    }
    loadAlerts();
    }, 300);

    return () => window.clearTimeout(timeoutId);
    }, [refreshAlerts]);

  useEffect(() => {
    const intervalId = window.setInterval(async () => {
      try {
        await refreshAlerts();
      } catch (err) {
        console.error("Alerts polling failed", err);
      }
    }, 60000);

    return () => window.clearInterval(intervalId);
  }, [refreshAlerts]);

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
        <select value={typeFilter} onChange={e => setTypeFilter(e.target.value as FilterAlertType)}>
          <option value="ALL">All types</option>
          <option value="SCORE_OVER">Score over</option>
          <option value="SCORE_UNDER">Score under</option>
        </select>
        <select value={periodFilter} onChange={e => setPeriodFilter(e.target.value as FilterPeriod)}>
          <option value="ALL">Any time</option>
          <option value="daily">Daily</option>
          <option value="weekly">Weekly</option>
          <option value="monthly">Monthly</option>
          <option value="yearly">Yearly</option>
        </select>
        <input
          value={teamFilter}
          onChange={e => setTeamFilter(e.target.value)}
          placeholder="Team name"
        />
      </div>

      {alerts.length === 0 ? (
        <p className="empty-state">No alerts found.</p>
      ) : (
        <ul className="alerts-list">
        {alerts.map(alert => (
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
