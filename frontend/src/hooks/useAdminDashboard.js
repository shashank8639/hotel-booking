import { useCallback, useEffect, useState } from 'react';
import { adminReportService } from '../services/adminReportService';
import { currentYearMonth, defaultReportRange } from '../utils/adminDates';

/**
 * Loads dashboard KPIs + chart datasets in parallel.
 */
export function useAdminDashboard() {
  const [summary, setSummary] = useState(null);
  const [revenue, setRevenue] = useState(null);
  const [occupancy, setOccupancy] = useState(null);
  const [bookings, setBookings] = useState(null);
  const [payments, setPayments] = useState(null);
  const [monthly, setMonthly] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    const range = defaultReportRange(30);
    const ym = currentYearMonth();
    try {
      const [dash, rev, occ, book, pay, mon] = await Promise.all([
        adminReportService.getDashboard(),
        adminReportService.getRevenue({ ...range, period: 'DAILY' }),
        adminReportService.getOccupancy(range),
        adminReportService.getBookings(range),
        adminReportService.getPayments(range),
        adminReportService.getMonthly(ym),
      ]);
      setSummary(dash);
      setRevenue(rev);
      setOccupancy(occ);
      setBookings(book);
      setPayments(pay);
      setMonthly(mon);
    } catch (err) {
      setError(err.message || 'Failed to load dashboard');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  return { summary, revenue, occupancy, bookings, payments, monthly, loading, error, reload: load };
}
