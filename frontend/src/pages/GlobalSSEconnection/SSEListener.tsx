import { useEffect, useState } from "react";
import { BASE_URL, clearJwtToken, getJwtToken, getUserId } from "../../api/utils";
import { Outlet } from "react-router-dom";
import { ProtectedRoute } from "../protectionRouter/router";
import { FiBell, FiX } from "react-icons/fi";

const SEEN_ALERT_EVENT_IDS_KEY = "seenAlertEventIds";
const MAX_SEEN_ALERT_EVENTS = 100;

function getSeenAlertEventIds() {
    try {
        const saved = localStorage.getItem(SEEN_ALERT_EVENT_IDS_KEY);
        return new Set<string>(saved ? JSON.parse(saved) : []);
    } catch {
        return new Set<string>();
    }
}

function saveSeenAlertEventIds(seenIds: Set<string>) {
    const latestIds = [...seenIds].slice(-MAX_SEEN_ALERT_EVENTS);
    localStorage.setItem(SEEN_ALERT_EVENT_IDS_KEY, JSON.stringify(latestIds));
}

type AlertNotification = {
    id?: string;
    teamName: string;
};

function readSseMessage(message: string): AlertNotification | null {
    const id = message
        .split("\n")
        .find(line => line.startsWith("id:"))
        ?.slice(3)
        .trim();

    const data = message
        .split("\n")
        .filter(line => line.startsWith("data:"))
        .map(line => line.slice(5).trimStart())
        .join("\n");

    if (!data) return null;
    if (id) {
        const seenIds = getSeenAlertEventIds();
        if (seenIds.has(id)) return null;

        seenIds.add(id);
        saveSeenAlertEventIds(seenIds);
    }

    const alertData = JSON.parse(data);
    return {
        id,
        teamName: alertData.teamName,
    };
}

function GlobalSSEListener({ onAlert }: { onAlert: (alert: AlertNotification) => void })
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
                        messages.forEach(message => {
                            const alertNotification = readSseMessage(message);
                            if (alertNotification) {
                                onAlert(alertNotification);
                            }
                        });
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
    }, [onAlert]);

    return null;
}

export default function ProtectedSSELayout() {
  const [notification, setNotification] = useState<AlertNotification | null>(null);

  return (
    <ProtectedRoute>
      <GlobalSSEListener onAlert={setNotification} />
      {notification && (
        <div className="alert-banner" role="status" aria-live="polite">
          <FiBell className="alert-banner-icon" aria-hidden="true" />
          <div className="alert-banner-content">
            <strong>Alert triggered</strong>
            <span>{notification.teamName}</span>
          </div>
          <button
            className="alert-banner-dismiss"
            type="button"
            aria-label="Dismiss alert"
            title="Dismiss alert"
            onClick={() => setNotification(null)}
          >
            <FiX aria-hidden="true" />
          </button>
        </div>
      )}
      <Outlet />
    </ProtectedRoute>
  );
}
