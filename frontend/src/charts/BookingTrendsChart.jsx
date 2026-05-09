import {
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import ChartCard from './ChartCard';
import { toChartRows } from '../utils/adminDates';

/** Bookings by check-in date from BookingReportResponse.byCheckInDate */
export default function BookingTrendsChart({ series, loading }) {
  const data = toChartRows(series, { valueKey: 'count' }).map((r) => ({
    ...r,
    value: r.count || r.amount,
  }));
  return (
    <ChartCard
      title="Booking trends"
      subtitle="Bookings grouped by check-in date"
      loading={loading}
    >
      <ResponsiveContainer width="100%" height={280}>
        <BarChart data={data}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="name" hide={data.length > 14} />
          <YAxis allowDecimals={false} />
          <Tooltip />
          <Bar dataKey="value" fill="#2e7d32" radius={[4, 4, 0, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </ChartCard>
  );
}
