/**
 * JWT / session storage helpers.
 *
 * Storage options (interview topic):
 * - localStorage: survives browser restart; XSS can steal tokens; good for "Remember me"
 * - sessionStorage: cleared when tab closes; still XSS-readable; better default for shared PCs
 * - memory only: safest vs XSS persistence, lost on refresh; pair with refresh cookie (httpOnly) in enterprise
 *
 * This app: Remember me → localStorage; otherwise → sessionStorage.
 * Access + refresh tokens are stored client-side (common SPA pattern). Prefer httpOnly cookies in high-security apps.
 */

const ACCESS_KEY = 'hb_access_token';
const REFRESH_KEY = 'hb_refresh_token';
const USER_KEY = 'hb_auth_user';
const REMEMBER_KEY = 'hb_remember_me';

function activeStore() {
  const remember = localStorage.getItem(REMEMBER_KEY) === 'true';
  return remember ? localStorage : sessionStorage;
}

function clearBoth() {
  [localStorage, sessionStorage].forEach((store) => {
    store.removeItem(ACCESS_KEY);
    store.removeItem(REFRESH_KEY);
    store.removeItem(USER_KEY);
  });
}

export const tokenStorage = {
  setRememberMe(remember) {
    localStorage.setItem(REMEMBER_KEY, remember ? 'true' : 'false');
  },

  isRememberMe() {
    return localStorage.getItem(REMEMBER_KEY) === 'true';
  },

  saveSession({ accessToken, refreshToken, user }, rememberMe = false) {
    this.setRememberMe(rememberMe);
    clearBoth();
    const store = rememberMe ? localStorage : sessionStorage;
    store.setItem(ACCESS_KEY, accessToken);
    if (refreshToken) {
      store.setItem(REFRESH_KEY, refreshToken);
    }
    if (user) {
      store.setItem(USER_KEY, JSON.stringify(user));
    }
  },

  getAccessToken() {
    return localStorage.getItem(ACCESS_KEY) || sessionStorage.getItem(ACCESS_KEY);
  },

  getRefreshToken() {
    return localStorage.getItem(REFRESH_KEY) || sessionStorage.getItem(REFRESH_KEY);
  },

  getUser() {
    const raw = localStorage.getItem(USER_KEY) || sessionStorage.getItem(USER_KEY);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw);
    } catch {
      return null;
    }
  },

  setAccessToken(accessToken) {
    const store = activeStore();
    // Keep refresh/user in the same store as current session
    const refresh = this.getRefreshToken();
    const user = this.getUser();
    clearBoth();
    store.setItem(ACCESS_KEY, accessToken);
    if (refresh) {
      store.setItem(REFRESH_KEY, refresh);
    }
    if (user) {
      store.setItem(USER_KEY, JSON.stringify(user));
    }
  },

  updateUser(user) {
    const store =
      localStorage.getItem(ACCESS_KEY) || localStorage.getItem(USER_KEY)
        ? localStorage
        : sessionStorage;
    store.setItem(USER_KEY, JSON.stringify(user));
  },

  clear() {
    clearBoth();
    localStorage.removeItem(REMEMBER_KEY);
  },
};
