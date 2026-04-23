import api from './api';

/** Admin room mutations under /admin/rooms (ROLE_ADMIN). */
export const adminRoomService = {
  create(payload) {
    return api.post('/admin/rooms', payload).then((r) => r.data);
  },

  update(id, payload) {
    return api.put(`/admin/rooms/${id}`, payload).then((r) => r.data);
  },

  remove(id) {
    return api.delete(`/admin/rooms/${id}`).then((r) => r.data);
  },

  updateAvailability(id, status) {
    return api.put(`/admin/rooms/${id}/availability`, { status }).then((r) => r.data);
  },

  updatePricing(id, payload) {
    return api.put(`/admin/rooms/${id}/pricing`, payload).then((r) => r.data);
  },

  addImage(id, payload) {
    return api.post(`/admin/rooms/${id}/images`, payload).then((r) => r.data);
  },

  deleteImage(roomId, imageId) {
    return api.delete(`/admin/rooms/${roomId}/images/${imageId}`);
  },
};
