import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
type Game = {
    id: number;
    homeTeam : string;
    homeScore : number;
    awayTeam : string;
    awayScore : number;
    schedTime : any;
    status : string;
  }

async function getAllgames() : Promise<Game[]>
{
  const res = await fetch("http://localhost:8080/games?status=LIVE&status=SCHEDULED");

  if (!res.ok) {
    throw new Error("Failed to fetch games");
  }

  const games: Game[] = await res.json();
  return games;
}

function GamesPage() {
  const [games, setGames] = useState<Game[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  useEffect(() => {
    async function loadGames() {
      try {
        const data = await getAllgames();

        setGames(data);
      }
      catch (err)
      {
        setError("Could not load games");
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


  if (loading) return <div>Loading games...</div>;
  if (error) return <div>{error}</div>;

  return (
    <div>
      <h1>Dashboard</h1>
      {games.map((game) => (
        <div key={game.id}>
          <p>
            {game.awayTeam} {game.awayScore} - {game.homeTeam} {game.homeScore}
          </p>
          <p>{game.status}</p>
          <p>{game.schedTime}</p>
          <Link to={`/games/${game.id}`}>
            <button>View Game</button>
          </Link>
        </div>
      ))}
    </div>
  );
}

export default GamesPage;