import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { apiFetch, getResponseErrorMessage } from "../../api/utils";
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
    <div>
      <h1>Dashboard</h1>
      <button onClick={() => setStatusFilter("SCHEDULED")}>
        Scheduled
      </button>
      <button onClick={() => setStatusFilter("LIVE")}>
        Live
      </button>
      <button onClick={() => setStatusFilter("FINAL")}>
        Final
      </button>
      {gamesFiltered.map((game) => (
        <div key={game.id}>
          <p>
            {game.awayTeam} {game.awayScore} - {game.homeTeam} {game.homeScore}
          </p>
          <p>{game.status}</p>
          <p>{game.schedTime}</p>
          <Link to={`/games/${game.id}`}>
            <button>View Game</button>
          </Link>
          <Link to={`/alerts/`}>

          </Link>
        </div>
      ))}
    </div>
  );
}

export default GamesPage;
