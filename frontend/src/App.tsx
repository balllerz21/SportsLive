import { Routes, Route } from "react-router-dom";
import GamesPage from "./pages/game/game";
import GameIdPage from "./pages/game/[id]/game[id]"

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