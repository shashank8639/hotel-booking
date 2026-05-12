import api from './api';

/** Booking Engine APIs — create/availability + admin list/status/cancel. */
export const bookingService = {
  create(payload) {
    return api.post('/bookings', payload).then((res) => res.data);
  },

  checkAvailability({ checkInDate, checkOutDate, roomIds }) {
    const params = new URLSearchParams();
    params.set('checkInDate', checkInDate);
    params.set('checkOutDate', checkOutDate);
    (roomIds || []).forEach((id) => params.append('roomIds', String(id)));
    return api.get(`/bookings/availability?${params.toString()}`).then((res) => res.data);
  },

  getById(id) {
    return api.get(`/bookings/${id}`).then((res) => res.data);
  },

  list({ page = 0, size = 10, sort = 'createdAt,desc' } = {}) {
    return api.get('/bookings', { params: { page, size, sort } }).then((res) => res.data);
  },

  listByStatus(status, { page = 0, size = 10 } = {}) {
    return api
      .get(`/bookings/status/${status}`, { params: { page, size } })
      .then((res) => res.data);
  },

  listByGuest(guestId, { page = 0, size = 10 } = {}) {
    return api
      .get(`/bookings/guest/${guestId}`, { params: { page, size } })
      .then((res) => res.data);
  },

  cancel(id) {
    return api.put(`/bookings/${id}/cancel`).then((res) => res.data);
  },

  updateStatus(id, status) {
    return api.put(`/bookings/${id}/status`, { status }).then((res) => res.data);
  },
};
