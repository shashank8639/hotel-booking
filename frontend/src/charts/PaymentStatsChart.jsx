import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip, Legend } from 'recharts';
import ChartCard from './ChartCard';
import { toChartRows } from '../utils/adminDates';

const COLORS = ['#2e7d32', '#ed6c02', '#d32f2f', '#1565c0', '#6a1b9a'];

/** Payment status distribution from PaymentReportResponse.byStatus */
export default function PaymentStatsChart({ series, loading }) {
  const data = toChartRows(series).map((r) => ({
    name: r.name,
    value: r.count || r.amount,
  }));
  return (
    <ChartCard title="Payment statistics" subtitle="Counts by payment status" loading={loading}>
      <ResponsiveContainer width="100%" height={280}>
        <PieChart>
          <Pie data={data} dataKey="value" nameKey="name" outerRadius={90} label>
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
