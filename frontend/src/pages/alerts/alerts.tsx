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

type PageResponse<T> = {
  content: T[];
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
};

const PAGE_SIZE = 50;

async function getAlerts(filters: AlertFilters, page: number) : Promise<PageResponse<AlertDto>>
{
    const params = new URLSearchParams();
    params.set('page', String(page));
    params.set('size', String(PAGE_SIZE));
    if (filters.status !== 'ALL') params.set('status', filters.status);
    if (filters.alertType !== 'ALL') params.set('alertType', filters.alertType);
    if (filters.teamName.trim()) params.set('teamName', filters.teamName.trim());
    if (filters.period !== 'ALL') params.set('period', filters.period);

    const query = params.toString();
    const res = await apiFetch(`/alerts${query ? `?${query}` : ""}`);
    if (!res.ok) {
        throw new Error(await getResponseErrorMessage(res));
    }
    return res.json() as Promise<PageResponse<AlertDto>>;
}

function AlertsList() {
  const [alerts, setAlerts] = useState<AlertDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [statusFilter, setStatusFilter] = useState<FilterStatus>('ALL');
  const [typeFilter, setTypeFilter] = useState<FilterAlertType>('ALL');
  const [teamFilter, setTeamFilter] = useState('');
  const [periodFilter, setPeriodFilter] = useState<FilterPeriod>('ALL');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const refreshAlerts = useCallback(async () => {
    const data = await getAlerts({
      status: statusFilter,
      alertType: typeFilter,
      teamName: teamFilter,
      period: periodFilter,
    }, page);
    setAlerts(data.content);
    setTotalPages(data.page.totalPages);
  }, [statusFilter, typeFilter, teamFilter, periodFilter, page]);

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
        <button className={statusFilter === "ALL" ? "is-active" : ""} onClick={() => { setStatusFilter("ALL"); setPage(0); }}>
          All
        </button>
        <button className={statusFilter === "CREATED" ? "is-active" : ""} onClick={() => { setStatusFilter("CREATED"); setPage(0); }}>
          Active
        </button>
        <button className={statusFilter === "TRIGGERED" ? "is-active" : ""} onClick={() => { setStatusFilter("TRIGGERED"); setPage(0); }}>
          Triggered
        </button>
        <button className={statusFilter === "FINISHED" ? "is-active" : ""} onClick={() => { setStatusFilter("FINISHED"); setPage(0); }}>
          Finished
        </button>
        <select value={typeFilter} onChange={e => { setTypeFilter(e.target.value as FilterAlertType); setPage(0); }}>
          <option value="ALL">All types</option>
          <option value="SCORE_OVER">Score over</option>
          <option value="SCORE_UNDER">Score under</option>
        </select>
        <select value={periodFilter} onChange={e => { setPeriodFilter(e.target.value as FilterPeriod); setPage(0); }}>
          <option value="ALL">Any time</option>
          <option value="daily">Daily</option>
          <option value="weekly">Weekly</option>
          <option value="monthly">Monthly</option>
          <option value="yearly">Yearly</option>
        </select>
        <input
          value={teamFilter}
          onChange={e => { setTeamFilter(e.target.value); setPage(0); }}
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
      <nav className="alerts-pagination" aria-label="Alerts pages">
        <button type="button" disabled={loading || page === 0} onClick={() => setPage(current => current - 1)}>
          Previous
        </button>
        <span>Page {page + 1}</span>
        <button type="button" disabled={loading || page + 1 >= totalPages} onClick={() => setPage(current => current + 1)}>
          Next
        </button>
      </nav>
    </main>
  );
}
export default AlertsList
