import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { apiFetch, formatUserDateTime, getResponseErrorMessage } from "../../api/utils";
type GameStatus = 'SCHEDULED' | 'LIVE' | 'FINAL';
type Game = {
    id: number;
    homeTeam : string;
    homeScore : number;
    awayTeam : string;
    awayScore : number;
    schedTime : string;
    status : GameStatus;
    updatedTime: string;
  }

const PAGE_SIZE = 50;

async function getAllgames(page: number, status?: GameStatus) : Promise<Game[]>
{
  const query = new URLSearchParams({
    size: String(PAGE_SIZE),
    page: String(page),
    sort: "updatedTime,desc",
  });
  if (status) {
    query.set("status", status);
  }
  const res = await apiFetch(`/games?${query}`);

  if (!res.ok) {
    throw new Error(await getResponseErrorMessage(res));
  }

  const games: Game[] = await res.json();
  return games;
}
type FilterStatus = GameStatus | 'ALL';
function GamesPage() {
  const [games, setGames] = useState<Game[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [statusFilter, setStatusFilter] = useState<FilterStatus>('ALL');
  const [page, setPage] = useState(0);

  const selectStatus = (status: FilterStatus) => {
    setStatusFilter(status);
    setPage(0);
  };

  useEffect(() => {
    async function loadGames() {
      setLoading(true);
      setError(null);
      try {
        const data = await getAllgames(page, statusFilter === "ALL" ? undefined : statusFilter);

        setGames(data);
      }
      catch (err)
      {
        const message = err instanceof Error ? err.message : "Could not load games";
        setError(message);
      }
      finally {
        setLoading(false);
      }
    }
    loadGames();
  }, [page, statusFilter]);

  useEffect(() => {
    const pollScores = async () => {
      try {
        const freshData = await getAllgames(page, statusFilter === "ALL" ? undefined : statusFilter);
        setGames(freshData);
      } catch (err) {
        console.error("Polling failed", err);
      }
    };

    const intervalId = setInterval(pollScores, 60000);

    return () => clearInterval(intervalId);
  }, [page, statusFilter]);

  if (loading) return <div>Loading games...</div>;
  if (error) return <div>{error || "Games failed to load..."}</div>;


  return (
    <main className="games-page">
      <div className="games-dashboard">
        <h1>Games Page</h1>
        <nav className="games-nav">
          <Link to="/dashboard"> Back to Dashboard </Link>
        </nav>
      </div>
      <div className="games-filter-bar">
        <button className={statusFilter === "ALL" ? "is-active" : ""} onClick={() => selectStatus("ALL")}>
          All
        </button>
        <button className={statusFilter === "SCHEDULED" ? "is-active" : ""} onClick={() => selectStatus("SCHEDULED")}>
          Scheduled
        </button>
        <button className={statusFilter === "LIVE" ? "is-active" : ""} onClick={() => selectStatus("LIVE")}>
          Live
        </button>
        <button className={statusFilter === "FINAL" ? "is-active" : ""} onClick={() => selectStatus("FINAL")}>
          Final
        </button>
      </div>
      <div className="games-list">
        {games.map((game) => (
          <div className="game-list-card" key={game.id}>
            <div className="game-list-matchup">
              <span>{game.awayTeam}</span>
              <strong>{game.awayScore} - {game.homeScore}</strong>
              <span>{game.homeTeam}</span>
            </div>
            <div className="game-list-meta">
              <span className={`status-pill status-${game.status.toLowerCase()}`}>{game.status}</span>
              <span>{game.status == 'SCHEDULED' ? formatUserDateTime(game.schedTime) : formatUserDateTime(game.updatedTime)}</span>
            </div>
            <Link className="game-card-action" to={`/games/${game.id}`}>
              View Game
            </Link>
          </div>
        ))}
      </div>
      <nav className="games-pagination" aria-label="Games pages">
        <button type="button" disabled={page === 0} onClick={() => setPage(current => current - 1)}>
          Previous
        </button>
        <span>Page {page + 1}</span>
        <button type="button" disabled={games.length < PAGE_SIZE} onClick={() => setPage(current => current + 1)}>
          Next
        </button>
      </nav>
    </main>
  );
}

export default GamesPage;
