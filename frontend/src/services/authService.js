import api from './api';

/**
 * Thin auth API client — maps 1:1 to Spring Boot AuthController.
 */
export const authService = {
  login(payload) {
    return api.post('/auth/login', payload).then((res) => res.data);
  },

  register(payload) {
    return api.post('/auth/register', payload).then((res) => res.data);
  },

  refresh(refreshToken) {
    return api.post('/auth/refresh', { refreshToken }).then((res) => res.data);
  },

  logout(refreshToken) {
    return api.post('/auth/logout', refreshToken ? { refreshToken } : {}).then((res) => res.data);
  },

  me() {
    return api.get('/auth/me').then((res) => res.data);
  },
};
