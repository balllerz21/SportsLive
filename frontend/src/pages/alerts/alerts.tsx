import { useState, useEffect} from 'react';
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
    const res = await fetch("http://localhost:8080/alerts");
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