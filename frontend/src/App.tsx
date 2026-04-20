import { useState, useEffect } from 'react'
import './App.css'

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
  const res = await fetch("http://localhost:8080/games");

  if (!res.ok) {
    throw new Error("Failed to fetch games");
  }

  const games: Game[] = await res.json();
  // add 
  return games;
}

function App() {
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

          <button
            onClick={() => {
              window.location.href = `/alerts/create?gameId=${game.id}`;
            }}
          >
            Create Alert
          </button>
        </div>
      ))}
    </div>
  );
}

export default App
