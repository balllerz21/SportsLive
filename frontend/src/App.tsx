import { Routes, Route } from "react-router-dom";
import GamesPage from "./pages/game/game";
import GameIdPage from "./pages/game/[id]/game[id]"
import AlertsList from "./pages/alerts/alerts";

function App() {
  return (
    <Routes>
      <Route path="/" element={<GamesPage />} />
      <Route path="/games" element={<GamesPage />} />
      <Route path="/games/:gameId" element={<GameIdPage />} />
      < Route path="/alerts" element={< AlertsList />} />
    </Routes>
  );
}

export default App;