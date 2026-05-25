import { addDaysIso, todayIso } from './format';

/** Default analytics window: last 30 days inclusive of today. */
export function defaultReportRange(days = 30) {
  const endDate = todayIso();
  const startDate = addDaysIso(endDate, -(days - 1));
  return { startDate, endDate };
}

export function currentYearMonth() {
  const d = new Date();
  return { year: d.getFullYear(), month: d.getMonth() + 1 };
}

/** Map LabeledAmountDto[] → Recharts-friendly rows. */
export function toChartRows(items = [], { valueKey = 'amount' } = {}) {
  return (items || []).map((item) => ({
    name: item.label ?? '—',
    amount: Number(item.amount ?? 0),
    count: Number(item.count ?? 0),
    value: Number(item[valueKey] ?? item.amount ?? item.count ?? 0),
  }));
}

export function occupancyPercent(summary) {
  const total = Number(summary?.totalRooms ?? 0);
  const occupied = Number(summary?.occupiedRooms ?? 0);
  if (!total) return 0;
  return Math.round((occupied / total) * 1000) / 10;
}
