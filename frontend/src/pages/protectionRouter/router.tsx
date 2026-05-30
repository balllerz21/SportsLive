import { Navigate } from "react-router-dom";
import type { ReactNode } from "react";
import { getJwtToken } from "../../api/utils";

export function ProtectedRoute({ children }: { children: ReactNode }) {
  const token = getJwtToken();
  return token ? <>{children}</> : <Navigate to="/" replace />;
}
