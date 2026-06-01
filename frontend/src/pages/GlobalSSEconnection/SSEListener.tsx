import { useEffect } from "react";
import { BASE_URL, clearJwtToken, getJwtToken, getUserId } from "../../api/utils";
import { Outlet } from "react-router-dom";
import { ProtectedRoute } from "../protectionRouter/router";

function readSseMessage(message: string) {
    const data = message
        .split("\n")
        .filter(line => line.startsWith("data:"))
        .map(line => line.slice(5).trimStart())
        .join("\n");

    if (!data) return;

    const alertData = JSON.parse(data);
    alert(`Alert triggered: ${alertData.teamName}`);
}

function GlobalSSEListener()
{
    useEffect(() => {
        const userId = getUserId();
        const token = getJwtToken();
        if (!userId || !token) return;

        const controller = new AbortController();

        async function connect() {
            while (!controller.signal.aborted) {
                try {
                    const res = await fetch(`${BASE_URL}/sse?clientId=${userId}`, {
                        headers: {
                            Accept: "text/event-stream",
                            Authorization: `Bearer ${token}`,
                        },
                        signal: controller.signal,
                    });

                    if (res.status === 401 || res.status === 403) {
                        clearJwtToken();
                        window.location.href = "/";
                        return;
                    }

                    if (!res.ok || !res.body) {
                        throw new Error("SSE connection failed");
                    }

                    const reader = res.body.getReader();
                    const decoder = new TextDecoder();
                    let buffer = "";

                    while (!controller.signal.aborted) {
                        const { value, done } = await reader.read();
                        if (done) break;

                        buffer += decoder.decode(value, { stream: true });
                        const messages = buffer.split(/\r?\n\r?\n/);
                        buffer = messages.pop() ?? "";
                        messages.forEach(readSseMessage);
                    }
                } catch {
                    if (controller.signal.aborted) return;
                    await new Promise(resolve => setTimeout(resolve, 3000));
                }
            }
        }

        connect();

        return () => {
            controller.abort();
        };
    }, []);

    return null;
}
export default function ProtectedSSELayout() {
  return (
    <ProtectedRoute>
      <GlobalSSEListener />
      <Outlet />
    </ProtectedRoute>
  );
}
