import api from './api';

/**
 * Guest APIs — booking upsert helpers + admin list/search/delete.
 */
export const guestService = {
  list({ page = 0, size = 10, sort = 'lastName,asc' } = {}) {
    return api.get('/guests', { params: { page, size, sort } }).then((r) => r.data);
  },

  getById(id) {
    return api.get(`/guests/${id}`).then((r) => r.data);
  },

  searchByEmail(email) {
    return api.get('/guests/search/email', { params: { email } }).then((r) => r.data);
  },

  searchByPhone(phone) {
    return api.get('/guests/search/phone', { params: { phone } }).then((r) => r.data);
  },

  searchByName(name) {
    return api.get('/guests/search/name', { params: { name } }).then((r) => r.data);
  },

  create(payload) {
    return api.post('/guests', payload).then((r) => r.data);
  },

  update(id, payload) {
    return api.put(`/guests/${id}`, payload).then((r) => r.data);
  },

  remove(id) {
    return api.delete(`/guests/${id}`);
  },

  async upsertFromForm(form) {
    const payload = {
      firstName: form.firstName.trim(),
      lastName: form.lastName.trim(),
      email: form.email.trim(),
      phone: form.phone.trim(),
    };

    try {
      const existing = await this.searchByEmail(payload.email);
      return this.update(existing.id, payload);
    } catch (err) {
      if (err.status !== 404) {
        throw err;
      }
      return this.create(payload);
    }
  },

  async findOrCreateFromUser(user) {
    return this.upsertFromForm({
      firstName: user.firstName || 'Guest',
      lastName: user.lastName || 'User',
      email: user.email,
      phone: '+91 9000000000',
    });
  },
};
