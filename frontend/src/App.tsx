import { Routes, Route } from "react-router-dom";
import GamesPage from "./game/game";
import GameIdPage from "./game/[id]/game[id]"

function App() {
  return (
    <Routes>
      <Route path="/" element={<GamesPage />} />
      <Route path="/games" element={<GamesPage />} />
      <Route path="/games/:gameId" element={<GameIdPage />} />
    </Routes>
  );
}

export default App;