/**
 * Persist admin report date filters across refresh (session only).
 */
export const REPORT_RANGE_KEY = 'hb_admin_report_range';

export function loadReportRange(fallback) {
  try {
    const raw = sessionStorage.getItem(REPORT_RANGE_KEY);
    if (!raw) return fallback;
    const parsed = JSON.parse(raw);
    return {
      startDate: parsed.startDate || fallback.startDate,
      endDate: parsed.endDate || fallback.endDate,
      year: parsed.year ?? fallback.year,
      month: parsed.month ?? fallback.month,
    };
  } catch {
    return fallback;
  }
}

export function saveReportRange(range) {
  if (typeof sessionStorage === 'undefined') return;
  sessionStorage.setItem(REPORT_RANGE_KEY, JSON.stringify(range));
}
