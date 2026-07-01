import {
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import ChartCard from './ChartCard';
import { toChartRows } from '../utils/adminDates';

/** Daily/period revenue series from RevenueReportResponse.series */
export default function MonthlyRevenueChart({ series, loading }) {
  const data = toChartRows(series);
  return (
    <ChartCard
      title="Monthly revenue trend"
      subtitle="Successful payment amounts by period (Module 9 revenue report)"
      loading={loading}
    >
      <ResponsiveContainer width="100%" height={280}>
        <LineChart data={data}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="name" hide={data.length > 14} />
          <YAxis />
          <Tooltip />
          <Line type="monotone" dataKey="amount" stroke="#1565c0" strokeWidth={2} dot={false} />
        </LineChart>
      </ResponsiveContainer>
    </ChartCard>
  );
}
