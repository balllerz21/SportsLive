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
    schedTime : any;
    status : GameStatus
  }

async function getAllgames() : Promise<Game[]>
{
  const res = await apiFetch("/games");

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
  useEffect(() => {
    async function loadGames() {
      try {
        const data = await getAllgames();

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
  }, []); 

  useEffect(() => {
    const pollScores = async () => {
      try {
        const freshData = await getAllgames();
        
        setGames(prevGames => 
          prevGames.map(oldGame => {
            const updated = freshData.find(g => g.id === oldGame.id);
            return updated ? { 
              ...oldGame, 
              homeScore: updated.homeScore, 
              awayScore: updated.awayScore,
              status: updated.status 
            } : oldGame;
          })
        );
      } catch (err) {
        console.error("Polling failed", err);
      }
    };

    const intervalId = setInterval(pollScores, 60000); 
    
    return () => clearInterval(intervalId); 
  }, []); 

  const gamesFiltered = statusFilter === 'ALL' ? games : games.filter(g => g.status === statusFilter);
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
        <button className={statusFilter === "ALL" ? "is-active" : ""} onClick={() => setStatusFilter("ALL")}>
          All
        </button>
        <button className={statusFilter === "SCHEDULED" ? "is-active" : ""} onClick={() => setStatusFilter("SCHEDULED")}>
          Scheduled
        </button>
        <button className={statusFilter === "LIVE" ? "is-active" : ""} onClick={() => setStatusFilter("LIVE")}>
          Live
        </button>
        <button className={statusFilter === "FINAL" ? "is-active" : ""} onClick={() => setStatusFilter("FINAL")}>
          Final
        </button>
      </div>
      <div className="games-list">
        {gamesFiltered.map((game) => (
          <div className="game-list-card" key={game.id}>
            <div className="game-list-matchup">
              <span>{game.awayTeam}</span>
              <strong>{game.awayScore} - {game.homeScore}</strong>
              <span>{game.homeTeam}</span>
            </div>
            <div className="game-list-meta">
              <span className={`status-pill status-${game.status.toLowerCase()}`}>{game.status}</span>
              <span>{formatUserDateTime(game.schedTime)}</span>
            </div>
            <Link className="game-card-action" to={`/games/${game.id}`}>
              View Game
            </Link>
          </div>
        ))}
      </div>
    </main>
  );
}

export default GamesPage;
