import api from './api';

/**
 * Payment Management APIs — orders, verify, history, refund, invoices.
 */
export const paymentService = {
  createOrder(bookingId) {
    return api.post('/payments/create-order', { bookingId }).then((res) => res.data);
  },

  verify(payload) {
    return api.post('/payments/verify', payload).then((res) => res.data);
  },

  refund(payload) {
    return api.post('/payments/refund', payload).then((res) => res.data);
  },

  history(filters = {}) {
    const params = {
      page: filters.page ?? 0,
      size: filters.size ?? 10,
      sort: filters.sort || 'createdAt,desc',
    };
    if (filters.bookingId) params.bookingId = filters.bookingId;
    if (filters.guestId) params.guestId = filters.guestId;
    if (filters.status) params.status = filters.status;
    if (filters.fromDate) params.fromDate = filters.fromDate;
    if (filters.toDate) params.toDate = filters.toDate;
    return api.get('/payments/history', { params }).then((res) => res.data);
  },

  getById(id) {
    return api.get(`/payments/${id}`).then((res) => res.data);
  },

  getInvoice(bookingId) {
    return api.get(`/payments/invoice/${bookingId}`).then((res) => res.data);
  },

  async downloadInvoicePdf(bookingId) {
    const response = await api.get(`/payments/invoice/pdf/${bookingId}`, {
      responseType: 'blob',
    });
    const blob = new Blob([response.data], { type: 'application/pdf' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `invoice-${bookingId}.pdf`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },
};
