import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip, Legend } from 'recharts';
import ChartCard from './ChartCard';
import { toChartRows } from '../utils/adminDates';

const COLORS = ['#ed6c02', '#2e7d32', '#1565c0', '#6a1b9a', '#d32f2f'];

/** Booking status breakdown from BookingReportResponse.byStatus */
export default function BookingStatusChart({ series, loading }) {
  const data = toChartRows(series).map((r) => ({
    name: r.name,
    value: r.count || r.amount,
  }));
  return (
    <ChartCard title="Booking status" subtitle="Volume by lifecycle status" loading={loading}>
      <ResponsiveContainer width="100%" height={280}>
        <PieChart>
          <Pie data={data} dataKey="value" nameKey="name" innerRadius={50} outerRadius={90}>
            {data.map((_, i) => (
              <Cell key={i} fill={COLORS[i % COLORS.length]} />
            ))}
          </Pie>
          <Tooltip />
          <Legend />
        </PieChart>
      </ResponsiveContainer>
    </ChartCard>
  );
}
