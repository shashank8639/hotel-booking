import api from './api';

/**
 * Room catalog APIs (GET /rooms/** is permitAll).
 * Admin mutations live in adminRoomService.
 */
export const roomService = {
  list(params = {}) {
    return api.get('/rooms', { params }).then((res) => res.data);
  },

  search(filters = {}) {
    const params = {};
    if (filters.roomNumber) params.roomNumber = filters.roomNumber;
    if (filters.roomType) params.roomType = filters.roomType;
    if (filters.status) params.status = filters.status;
    if (filters.minCapacity) params.minCapacity = filters.minCapacity;
    if (filters.minPrice !== '' && filters.minPrice != null) params.minPrice = filters.minPrice;
    if (filters.maxPrice !== '' && filters.maxPrice != null) params.maxPrice = filters.maxPrice;
    params.page = filters.page ?? 0;
    params.size = filters.size ?? 10;
    if (filters.sort) params.sort = filters.sort;

    return api.get('/rooms/search', { params }).then((res) => res.data);
  },

  getById(id) {
    return api.get(`/rooms/${id}`).then((res) => res.data);
  },

  getImages(id) {
    return api.get(`/rooms/${id}/images`).then((res) => res.data);
  },

  getTypes() {
    return api.get('/rooms/types').then((res) => res.data);
  },

  getStatuses() {
    return api.get('/rooms/statuses').then((res) => res.data);
  },
};
