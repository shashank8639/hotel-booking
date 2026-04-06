import { Bar, BarChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import ChartCard from './ChartCard';

/**
 * Guest registrations — monthly report exposes a single monthly count.
 * Chart shows current month KPI as a simple bar (enterprise often joins a time series later).
 */
export default function GuestRegistrationsChart({ monthly, loading }) {
  const data = [
    {
      name: monthly ? `${monthly.year}-${String(monthly.month).padStart(2, '0')}` : 'Month',
      value: Number(monthly?.monthlyGuestRegistrations ?? 0),
    },
  ];
  return (
    <ChartCard
      title="Guest registrations"
      subtitle="New guests counted in the selected month report"
      loading={loading}
    >
      <ResponsiveContainer width="100%" height={280}>
        <BarChart data={data}>
          <XAxis dataKey="name" />
          <YAxis allowDecimals={false} />
          <Tooltip />
          <Bar dataKey="value" fill="#00838f" radius={[4, 4, 0, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </ChartCard>
  );
}
