import {
  Area,
  AreaChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import ChartCard from './ChartCard';
import { toChartRows } from '../utils/adminDates';

/** Daily occupancy % from OccupancyReportResponse.dailyOccupancy */
export default function OccupancyChart({ series, loading, periodPercent }) {
  const data = toChartRows(series).map((r) => ({
    ...r,
    value: r.amount || r.count,
  }));
  return (
    <ChartCard
      title="Occupancy trends"
      subtitle={
        periodPercent != null
          ? `Period occupancy ${Number(periodPercent).toFixed(1)}%`
          : 'Booked room-nights vs capacity'
      }
      loading={loading}
    >
      <ResponsiveContainer width="100%" height={280}>
        <AreaChart data={data}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="name" hide={data.length > 14} />
          <YAxis />
          <Tooltip />
          <Area type="monotone" dataKey="value" stroke="#6a1b9a" fill="#ce93d8" />
        </AreaChart>
      </ResponsiveContainer>
    </ChartCard>
  );
}
