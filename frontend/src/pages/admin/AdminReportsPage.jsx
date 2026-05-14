import { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  MenuItem,
  Paper,
  Stack,
  Tab,
  Tabs,
  TextField,
  Typography,
} from '@mui/material';
import AdminPageHeader from '../../components/admin/AdminPageHeader';
import MonthlyRevenueChart from '../../charts/MonthlyRevenueChart';
import OccupancyChart from '../../charts/OccupancyChart';
import BookingStatusChart from '../../charts/BookingStatusChart';
import PaymentStatsChart from '../../charts/PaymentStatsChart';
import { AdminGrid, AdminGridItem } from '../../components/admin/AdminGrid';
import { adminReportService } from '../../services/adminReportService';
import { currentYearMonth, defaultReportRange } from '../../utils/adminDates';
import { loadReportRange, saveReportRange } from '../../utils/reportRangeStorage';
import { formatCurrency } from '../../utils/format';
import { useAdminUi } from '../../context/AdminUiContext';

/**
 * Reports hub — revenue, occupancy, bookings, monthly, payments.
 * Date range persisted in sessionStorage. Export buttons are UI-only.
 */
export default function AdminReportsPage() {
  const { notifySuccess } = useAdminUi();
  const defaults = {
    ...defaultReportRange(30),
    ...currentYearMonth(),
  };
  const initial = loadReportRange(defaults);
  const [tab, setTab] = useState(0);
  const [startDate, setStartDate] = useState(initial.startDate);
  const [endDate, setEndDate] = useState(initial.endDate);
  const [year, setYear] = useState(initial.year);
  const [month, setMonth] = useState(initial.month);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [revenue, setRevenue] = useState(null);
  const [occupancy, setOccupancy] = useState(null);
  const [bookings, setBookings] = useState(null);
  const [monthly, setMonthly] = useState(null);
  const [payments, setPayments] = useState(null);

  useEffect(() => {
    saveReportRange({ startDate, endDate, year, month });
  }, [startDate, endDate, year, month]);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      setLoading(true);
      setError('');
      try {
        const [rev, occ, book, mon, pay] = await Promise.all([
          adminReportService.getRevenue({ startDate, endDate, period: 'DAILY' }),
          adminReportService.getOccupancy({ startDate, endDate }),
          adminReportService.getBookings({ startDate, endDate }),
          adminReportService.getMonthly({ year, month }),
          adminReportService.getPayments({ startDate, endDate }),
        ]);
        if (cancelled) return;
        setRevenue(rev);
        setOccupancy(occ);
        setBookings(book);
        setMonthly(mon);
        setPayments(pay);
      } catch (err) {
        if (!cancelled) setError(err.message || 'Failed to load reports');
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    load();
    return () => {
      cancelled = true;
    };
  }, [startDate, endDate, year, month]);

  const exportUi = (name) => {
    notifySuccess(`${name} export is UI-only in this module (wire CSV/PDF next)`);
  };

  return (
    <Box data-testid="admin-reports-page">
      <AdminPageHeader
        title="Reports"
        subtitle="Analytics from Module 9 admin report APIs"
        actions={
          <Stack direction="row" spacing={1}>
            <Button variant="outlined" onClick={() => exportUi('Revenue')}>Export</Button>
          </Stack>
        }
      />

      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} sx={{ mb: 2 }}>
        <TextField
          size="small"
          type="date"
          label="Start"
          InputLabelProps={{ shrink: true }}
          value={startDate}
          onChange={(e) => setStartDate(e.target.value)}
        />
        <TextField
          size="small"
          type="date"
          label="End"
          InputLabelProps={{ shrink: true }}
          value={endDate}
          onChange={(e) => setEndDate(e.target.value)}
        />
        <TextField
          size="small"
          type="number"
          label="Year"
          value={year}
          onChange={(e) => setYear(Number(e.target.value))}
          sx={{ width: 100 }}
        />
        <TextField
          size="small"
          select
          label="Month"
          value={month}
          onChange={(e) => setMonth(Number(e.target.value))}
          sx={{ width: 120 }}
        >
          {Array.from({ length: 12 }, (_, i) => i + 1).map((m) => (
            <MenuItem key={m} value={m}>{m}</MenuItem>
          ))}
        </TextField>
      </Stack>

      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 2 }} variant="scrollable">
        <Tab label="Revenue" />
        <Tab label="Occupancy" />
        <Tab label="Bookings" />
        <Tab label="Monthly" />
        <Tab label="Payments" />
      </Tabs>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {loading && <CircularProgress sx={{ mb: 2 }} />}

      {tab === 0 && (
        <Stack spacing={2}>
          <Paper variant="outlined" sx={{ p: 2 }}>
            <Typography fontWeight={700}>Revenue report</Typography>
            <Typography>Total: {formatCurrency(revenue?.totalRevenue)}</Typography>
            <Typography>Refunds: {formatCurrency(revenue?.totalRefunds)}</Typography>
            <Typography>Net: {formatCurrency(revenue?.netRevenue)}</Typography>
            <Button sx={{ mt: 1 }} size="small" onClick={() => exportUi('Revenue')}>Export revenue</Button>
          </Paper>
          <MonthlyRevenueChart series={revenue?.series} loading={loading} />
        </Stack>
      )}

      {tab === 1 && (
        <Stack spacing={2}>
          <Paper variant="outlined" sx={{ p: 2 }}>
            <Typography fontWeight={700}>Occupancy report</Typography>
            <Typography>Period: {Number(occupancy?.periodOccupancyPercent ?? 0).toFixed(1)}%</Typography>
            <Typography>Current: {Number(occupancy?.currentOccupancyPercent ?? 0).toFixed(1)}%</Typography>
            <Button sx={{ mt: 1 }} size="small" onClick={() => exportUi('Occupancy')}>Export occupancy</Button>
          </Paper>
          <OccupancyChart
            series={occupancy?.dailyOccupancy}
            periodPercent={occupancy?.periodOccupancyPercent}
            loading={loading}
          />
        </Stack>
      )}

      {tab === 2 && (
        <Stack spacing={2}>
          <Paper variant="outlined" sx={{ p: 2 }}>
            <Typography fontWeight={700}>Booking report</Typography>
            <Typography>Total: {bookings?.totalBookings ?? 0}</Typography>
            <Typography>Cancelled: {bookings?.cancelledBookings ?? 0}</Typography>
            <Typography>Completed: {bookings?.completedBookings ?? 0}</Typography>
            <Button sx={{ mt: 1 }} size="small" onClick={() => exportUi('Bookings')}>Export bookings</Button>
          </Paper>
          <BookingStatusChart series={bookings?.byStatus} loading={loading} />
        </Stack>
      )}

      {tab === 3 && (
        <Paper variant="outlined" sx={{ p: 2 }}>
          <Typography fontWeight={700} gutterBottom>Monthly consolidated</Typography>
          <Typography>Revenue: {formatCurrency(monthly?.monthlyRevenue)}</Typography>
          <Typography>Bookings: {monthly?.monthlyBookings ?? 0}</Typography>
          <Typography>Cancellations: {monthly?.monthlyCancellations ?? 0}</Typography>
          <Typography>Guest registrations: {monthly?.monthlyGuestRegistrations ?? 0}</Typography>
          <Typography>Occupancy: {Number(monthly?.monthlyOccupancyPercent ?? 0).toFixed(1)}%</Typography>
          <Button sx={{ mt: 1 }} size="small" onClick={() => exportUi('Monthly')}>Export monthly</Button>
        </Paper>
      )}

      {tab === 4 && (
        <Stack spacing={2}>
          <Paper variant="outlined" sx={{ p: 2 }}>
            <Typography fontWeight={700}>Payment report</Typography>
            <Typography>Collected: {formatCurrency(payments?.totalCollected)}</Typography>
            <Typography>Refunded: {formatCurrency(payments?.totalRefunded)}</Typography>
            <Typography>Count: {payments?.paymentCount ?? 0}</Typography>
            <Button sx={{ mt: 1 }} size="small" onClick={() => exportUi('Payments')}>Export payments</Button>
          </Paper>
          <AdminGrid minWidth={320}>
            <AdminGridItem>
              <PaymentStatsChart series={payments?.byStatus} loading={loading} />
            </AdminGridItem>
          </AdminGrid>
        </Stack>
      )}
    </Box>
  );
}
