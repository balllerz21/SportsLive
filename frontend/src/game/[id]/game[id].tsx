import { useParams } from "react-router-dom";
import { useEffect, useState } from "react";
type Alert =
{}
type Game = {
    id: number;
    homeTeam : string;
    homeScore : number;
    awayTeam : string;
    awayScore : number;
    schedTime : any;
    status : string;
    alerts : Alert[]
  }

async function getGameById(id : number) : Promise<Game>{
  const res = await fetch(`http://localhost:8080/games/${id}`)
  if (!res.ok) {
    throw new Error("Failed to fetch games");
  }
  const game: Game = await res.json();
  return game;
}
function GamePage()
{
    const { gameId } = useParams<{ gameId: string }>();
    const [game, setGame] = useState<Game | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
      useEffect(() => {
        async function loadGame() {
          try {
            if (!gameId) return;
            let temp = (gameId as unknown)
            let id = (temp as number)
            const data = await getGameById(id);
            setGame(data);
            console.log(data)
          }
          catch (err)
          {
            setError("Could not load games");
          }
          finally {
            setLoading(false);
          }
        } 
        loadGame();
      }, [gameId]); 

  if (loading) return <div>Loading game...</div>;
  if (error || !game) return <div>{error || "Game not found"}</div>;

  return (
    <div>
      <h1>Game Details</h1>
      <div>
        <p>
          {game.awayTeam} {game.awayScore} - {game.homeTeam} {game.homeScore}
        </p>
        <p>Status: {game.status}</p>
        <p>Time: {game.schedTime}</p>
      </div>
    </div>
  );
}

export default GamePage