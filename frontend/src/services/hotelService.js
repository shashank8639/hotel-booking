import api from './api';

export const hotelService = {
  search(params = {}) {
    return api.get('/hotels/search', { params }).then((res) => res.data);
  },
  featured() {
    return api.get('/hotels/featured').then((res) => res.data);
  },
  getBySlug(slug) {
    return api.get(`/hotels/${slug}`).then((res) => res.data);
  },
  rooms(slug) {
    return api.get(`/hotels/${slug}/rooms`).then((res) => res.data);
  },
};

export const cityService = {
  list() {
    return api.get('/cities').then((res) => res.data);
  },
  popular() {
    return api.get('/cities/popular').then((res) => res.data);
  },
};
