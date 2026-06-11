import { Routes, Route } from "react-router-dom";
import GamesPage from "./pages/game/game";
import GameIdPage from "./pages/game/[id]/game[id]"
import AlertsList from "./pages/alerts/alerts";
import LoginPage from "./pages/login/login";
import SignUpPage from "./pages/signup/signup";
import DashboardPage from "./pages/dashboard/dashboard";
import ProtectedSSELayout from "./pages/GlobalSSEconnection/SSEListener";

function App() {
  return (
  <Routes>
    <Route path="/" element={<LoginPage />} />
    <Route path="/signup" element={<SignUpPage />} />

    <Route element={<ProtectedSSELayout />}>
      <Route path="/dashboard" element={<DashboardPage />} />
      <Route path="/games" element={<GamesPage />} />
      <Route path="/games/:gameId" element={<GameIdPage />} />
      <Route path="/alerts" element={<AlertsList />} />
    </Route>
  </Routes>
  );
}

export default App;
