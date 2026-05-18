import { useEffect, useRef, useState } from 'react';
import { adminReportService } from '../services/adminReportService';

const POLL_MS = 60_000;

/**
 * Challenge: poll GET /admin/dashboard every 60s for todaysCheckIns.
 * Uses AbortController so in-flight requests cancel on unmount / next tick.
 */
export function useTodaysCheckInsPoll(enabled = true, intervalMs = POLL_MS) {
  const [todaysCheckIns, setTodaysCheckIns] = useState(null);
  const [lastUpdated, setLastUpdated] = useState(null);
  const [pollError, setPollError] = useState('');
  const abortRef = useRef(null);

  useEffect(() => {
    if (!enabled) return undefined;

    let cancelled = false;

    const poll = async () => {
      abortRef.current?.abort();
      const controller = new AbortController();
      abortRef.current = controller;

      try {
        const dash = await adminReportService.getDashboard({ signal: controller.signal });
        if (cancelled || controller.signal.aborted) return;
        setTodaysCheckIns(dash.todaysCheckIns ?? 0);
        setLastUpdated(new Date().toISOString());
        setPollError('');
      } catch (err) {
        if (err.code === 'ERR_CANCELED' || err.name === 'CanceledError' || controller.signal.aborted) {
          return;
        }
        if (!cancelled) setPollError(err.message || 'Check-in poll failed');
      }
    };

    poll();
    const timer = setInterval(poll, intervalMs);

    return () => {
      cancelled = true;
      clearInterval(timer);
      abortRef.current?.abort();
    };
  }, [enabled, intervalMs]);

  return { todaysCheckIns, lastUpdated, pollError };
}
