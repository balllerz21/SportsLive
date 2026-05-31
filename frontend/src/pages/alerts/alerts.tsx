import { useState, useEffect} from 'react';
import { apiFetch, BASE_URL, getUserId } from '../../api/utils';
type AlertType = 'SCORE_ABOVE' | 'SCORE_BELOW';
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
        setError("Could not load games");
      }
      finally {
        setLoading(false);
      } 
    }
    loadAlerts();
    }, []);
    useEffect(() => {
      const userId = getUserId();
      if (!userId) return;                 

      const es = new EventSource(`${BASE_URL}/sse?clientId=${userId}`);
      es.addEventListener("ALERT", (e) => {
        const incoming: AlertDto = JSON.parse((e as MessageEvent).data);

        setAlerts(prev => {
          const exists = prev.some(a => a.id === incoming.id);
          return exists
            ? prev.map(a => (a.id === incoming.id ? incoming : a))
            : [incoming, ...prev];
        });
      });

      es.onerror = () => console.warn("SSE error — browser will retry");

      return () => es.close();                 
    }, []);

  const visibleAlerts = statusFilter === 'ALL' ? alerts : alerts.filter(a => a.status === statusFilter);

  if (loading) return <div>Loading...</div>;
  if(error) return <div>{error || "Alerts failed to load..."}</div>;

  return (
    <div>
      <button onClick={() => setStatusFilter("CREATED")}>
        Active
      </button>
      <button onClick={() => setStatusFilter("TRIGGERED")}>
        Triggered
      </button>
      <button onClick={() => setStatusFilter("FINISHED")}>
        Finished
      </button>
      <ul>
        {visibleAlerts.map(alert => (
          <li key={alert.id}>
            {alert.teamName} — {alert.status}
          </li>
        ))}
      </ul>
    </div>
  );
}
export default AlertsList