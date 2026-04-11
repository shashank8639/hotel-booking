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

/** Revenue by room type from RevenueReportResponse.byRoomType */
export default function RoomTypeChart({ series, loading }) {
  const data = toChartRows(series);
  return (
    <ChartCard
      title="Room type distribution"
      subtitle="Revenue contribution by room type"
      loading={loading}
    >
      <ResponsiveContainer width="100%" height={280}>
        <BarChart data={data} layout="vertical" margin={{ left: 24 }}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis type="number" />
          <YAxis type="category" dataKey="name" width={90} />
          <Tooltip />
          <Bar dataKey="amount" fill="#0277bd" radius={[0, 4, 4, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </ChartCard>
  );
}
