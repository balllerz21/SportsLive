import { Routes, Route } from "react-router-dom";
import GamesPage from "./pages/game/game";
import GameIdPage from "./pages/game/[id]/game[id]"
import AlertsList from "./pages/alerts/alerts";
import LoginPage from "./pages/login/login";
import SignUpPage from "./pages/signup/signup";
import { ProtectedRoute } from "./pages/protectionRouter/router";

function App() {
  return (
    <Routes>
      <Route path="/" element={<LoginPage />} />
      <Route path="/games" element={<ProtectedRoute><GamesPage /></ProtectedRoute>} />
      <Route path="/games/:gameId" element={<ProtectedRoute><GameIdPage /></ProtectedRoute>} />
      <Route path="/alerts" element={<ProtectedRoute><AlertsList /></ProtectedRoute>} />
      <Route path="/signup" element={<SignUpPage />} />
    </Routes>
  );
}

export default App;