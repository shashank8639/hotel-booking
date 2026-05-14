import api from './api';

/** Module 9 admin analytics — read-only report APIs. */
export const adminReportService = {
  getDashboard(config = {}) {
    return api.get('/admin/dashboard', config).then((r) => r.data);
  },

  getRevenue({ startDate, endDate, period = 'DAILY' }) {
    return api
      .get('/admin/reports/revenue', { params: { startDate, endDate, period } })
      .then((r) => r.data);
  },

  getOccupancy({ startDate, endDate }) {
    return api
      .get('/admin/reports/occupancy', { params: { startDate, endDate } })
      .then((r) => r.data);
  },

  getBookings({ startDate, endDate }) {
    return api
      .get('/admin/reports/bookings', { params: { startDate, endDate } })
      .then((r) => r.data);
  },

  getBookingsByStatus({ startDate, endDate, status }) {
    return api
      .get('/admin/reports/bookings/status', { params: { startDate, endDate, status } })
      .then((r) => r.data);
  },

  getMonthly({ year, month } = {}) {
    return api.get('/admin/reports/monthly', { params: { year, month } }).then((r) => r.data);
  },

  getPayments({ startDate, endDate, status }) {
    return api
      .get('/admin/reports/payments', { params: { startDate, endDate, status } })
      .then((r) => r.data);
  },
};
