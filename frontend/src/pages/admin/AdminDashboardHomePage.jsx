import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';
import AttachMoneyIcon from '@mui/icons-material/AttachMoney';
import TodayIcon from '@mui/icons-material/Today';
import EventAvailableIcon from '@mui/icons-material/EventAvailable';
import HotelIcon from '@mui/icons-material/Hotel';
import PeopleIcon from '@mui/icons-material/People';
import MeetingRoomIcon from '@mui/icons-material/MeetingRoom';
import PendingActionsIcon from '@mui/icons-material/PendingActions';
import PercentIcon from '@mui/icons-material/Percent';
import RefreshIcon from '@mui/icons-material/Refresh';
import LoginIcon from '@mui/icons-material/Login';
import AdminPageHeader from '../../components/admin/AdminPageHeader';
import StatCard from '../../components/admin/StatCard';
import QuickActions from '../../components/admin/QuickActions';
import { AdminGrid, AdminGridItem } from '../../components/admin/AdminGrid';
import MonthlyRevenueChart from '../../charts/MonthlyRevenueChart';
import BookingTrendsChart from '../../charts/BookingTrendsChart';
import OccupancyChart from '../../charts/OccupancyChart';
import PaymentStatsChart from '../../charts/PaymentStatsChart';
import RoomTypeChart from '../../charts/RoomTypeChart';
import BookingStatusChart from '../../charts/BookingStatusChart';
import GuestRegistrationsChart from '../../charts/GuestRegistrationsChart';
import { useAdminDashboard } from '../../hooks/useAdminDashboard';
import { useTodaysCheckInsPoll } from '../../hooks/useTodaysCheckInsPoll';
import { formatCurrency, formatDateLabel } from '../../utils/format';
import { occupancyPercent } from '../../utils/adminDates';

/**
 * Admin dashboard home — KPI cards + Recharts + refresh + live check-in poll.
 */
export default function AdminDashboardHomePage() {
  const { summary, revenue, occupancy, bookings, payments, monthly, loading, error, reload } =
    useAdminDashboard();
  const { todaysCheckIns, lastUpdated, pollError } = useTodaysCheckInsPoll(true, 60_000);

  if (loading && !summary) {
    return (
      <Box sx={{ py: 8, textAlign: 'center' }}>
        <CircularProgress />
        <Typography sx={{ mt: 2 }}>Loading dashboard…</Typography>
      </Box>
    );
  }

  if (error && !summary) {
    return <Alert severity="error">{error}</Alert>;
  }

  const occ = occupancyPercent(summary);
  const totalBookings = Number(bookings?.totalBookings ?? 0);
  const checkInsDisplay =
    todaysCheckIns != null ? todaysCheckIns : summary?.todaysCheckIns ?? 0;

  return (
    <Box data-testid="admin-dashboard-home">
      <AdminPageHeader
        title="Dashboard"
        subtitle="Live KPIs from GET /admin/dashboard and report endpoints"
        actions={
          <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            onClick={() => reload()}
            disabled={loading}
            data-testid="dashboard-refresh"
          >
            Refresh
          </Button>
        }
      />

      {error && (
        <Alert severity="warning" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}
      {pollError && (
        <Alert severity="info" sx={{ mb: 2 }}>
          Check-in poll: {pollError}
        </Alert>
      )}

      <Box sx={{ mb: 3 }}>
        <AdminGrid minWidth={220}>
          <StatCard
            title="Monthly revenue"
            value={formatCurrency(summary?.monthlyRevenue)}
            subtitle="Current month"
            icon={AttachMoneyIcon}
          />
          <StatCard
            title="Today's revenue"
            value={formatCurrency(summary?.todaysRevenue)}
            icon={TodayIcon}
            color="success.main"
          />
          <StatCard
            title="Total bookings"
            value={String(totalBookings)}
            subtitle="Last 30 days (report)"
            icon={EventAvailableIcon}
          />
          <StatCard
            title="Today's bookings"
            value={String(summary?.todaysBookings ?? 0)}
            icon={HotelIcon}
          />
          <StatCard
            title="Today's check-ins"
            value={String(checkInsDisplay)}
            subtitle={
              lastUpdated
                ? `Live poll · updated ${new Date(lastUpdated).toLocaleTimeString()}`
                : 'Polling every 60s'
            }
            icon={LoginIcon}
            color="info.main"
          />
          <StatCard
            title="Occupancy rate"
            value={`${occ}%`}
            subtitle={`${summary?.occupiedRooms ?? 0} / ${summary?.totalRooms ?? 0} rooms`}
            icon={PercentIcon}
            color="secondary.main"
          />
          <StatCard
            title="Total guests"
            value={String(summary?.totalGuests ?? 0)}
            icon={PeopleIcon}
          />
          <StatCard
            title="Available rooms"
            value={String(summary?.availableRooms ?? 0)}
            icon={MeetingRoomIcon}
            color="info.main"
          />
          <StatCard
            title="Pending payments"
            value={String(summary?.pendingPayments ?? 0)}
            icon={PendingActionsIcon}
            color="warning.main"
          />
        </AdminGrid>
      </Box>

      <Box sx={{ mb: 3 }}>
        <QuickActions />
      </Box>

      <AdminGrid minWidth={320}>
        <AdminGridItem>
          <MonthlyRevenueChart series={revenue?.series} loading={loading} />
        </AdminGridItem>
        <AdminGridItem>
          <BookingTrendsChart series={bookings?.byCheckInDate} loading={loading} />
        </AdminGridItem>
        <AdminGridItem>
          <OccupancyChart
            series={occupancy?.dailyOccupancy}
            periodPercent={occupancy?.periodOccupancyPercent}
            loading={loading}
          />
        </AdminGridItem>
        <AdminGridItem>
          <PaymentStatsChart series={payments?.byStatus} loading={loading} />
        </AdminGridItem>
        <AdminGridItem>
          <RoomTypeChart series={revenue?.byRoomType} loading={loading} />
        </AdminGridItem>
        <AdminGridItem>
          <BookingStatusChart series={bookings?.byStatus} loading={loading} />
        </AdminGridItem>
        <AdminGridItem>
          <GuestRegistrationsChart monthly={monthly} loading={loading} />
        </AdminGridItem>
      </AdminGrid>

      <Paper variant="outlined" sx={{ p: 2, mt: 3 }}>
        <Typography fontWeight={700} gutterBottom>
          Recent bookings
        </Typography>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Guest</TableCell>
              <TableCell>Check-in</TableCell>
              <TableCell>Status</TableCell>
              <TableCell align="right">Amount</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {(summary?.recentBookings || []).map((b) => (
              <TableRow
                key={b.id || `${b.guestName}-${b.checkInDate}`}
                sx={
                  b.status === 'CANCELLED'
                    ? { bgcolor: 'rgba(211, 47, 47, 0.08)', opacity: 0.9 }
                    : undefined
                }
              >
                <TableCell>{b.id ?? '—'}</TableCell>
                <TableCell>{b.guestName || b.guestEmail || '—'}</TableCell>
                <TableCell>{formatDateLabel(b.checkInDate)}</TableCell>
                <TableCell>{b.status}</TableCell>
                <TableCell align="right">{formatCurrency(b.totalAmount)}</TableCell>
              </TableRow>
            ))}
            {!summary?.recentBookings?.length && (
              <TableRow>
                <TableCell colSpan={5}>
                  <Typography color="text.secondary">No recent bookings</Typography>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </Paper>
    </Box>
  );
}
