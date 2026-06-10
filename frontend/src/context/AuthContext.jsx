import { createContext, useCallback, useEffect, useMemo, useState } from 'react';
import { authService } from '../services/authService';
import { registerUnauthorizedHandler } from '../services/api';
import { tokenStorage } from '../auth/tokenStorage';
import { getDefaultHomePath, normalizeRoles } from '../auth/roles';

/**
 * AuthContext centralizes login state for the whole SPA.
 * Why Context (not props): many distant components (Navbar, guards, pages) need the same user/token state.
 */
export const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [accessToken, setAccessToken] = useState(null);
  const [loading, setLoading] = useState(true);
  const [bootstrapped, setBootstrapped] = useState(false);

  const clearAuthState = useCallback(() => {
    tokenStorage.clear();
    setUser(null);
    setAccessToken(null);
  }, []);

  useEffect(() => {
    registerUnauthorizedHandler(() => {
      clearAuthState();
    });
  }, [clearAuthState]);

  /**
   * On first load: restore session from storage and optionally refresh user via /auth/me.
   */
  useEffect(() => {
    let cancelled = false;

    async function bootstrap() {
      const token = tokenStorage.getAccessToken();
      const storedUser = tokenStorage.getUser();

      if (!token) {
        if (!cancelled) {
          setLoading(false);
          setBootstrapped(true);
        }
        return;
      }

      setAccessToken(token);
      if (storedUser) {
        setUser(storedUser);
      }

      try {
        const me = await authService.me();
        if (!cancelled) {
          setUser(me);
          tokenStorage.updateUser(me);
        }
      } catch {
        // Access may be expired; interceptor may refresh. If still failing, clear.
        if (!tokenStorage.getAccessToken()) {
          clearAuthState();
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
          setBootstrapped(true);
        }
      }
    }

    bootstrap();
    return () => {
      cancelled = true;
    };
  }, [clearAuthState]);

  const applyAuthResponse = useCallback((data, rememberMe = tokenStorage.isRememberMe()) => {
    tokenStorage.saveSession(
      {
        accessToken: data.accessToken,
        refreshToken: data.refreshToken,
        user: data.user,
      },
      rememberMe
    );
    setAccessToken(data.accessToken);
    setUser(data.user);
    return data.user;
  }, []);

  const login = useCallback(
    async ({ email, password, rememberMe = false }) => {
      const data = await authService.login({ email, password });
      const nextUser = applyAuthResponse(data, rememberMe);
      return { user: nextUser, redirectTo: getDefaultHomePath(nextUser?.roles) };
    },
    [applyAuthResponse]
  );

  const register = useCallback(
    async (payload) => {
      const { rememberMe = false, ...body } = payload;
      const data = await authService.register(body);
      const nextUser = applyAuthResponse(data, rememberMe);
      return { user: nextUser, redirectTo: getDefaultHomePath(nextUser?.roles) };
    },
    [applyAuthResponse]
  );

  const logout = useCallback(async () => {
    const refreshToken = tokenStorage.getRefreshToken();
    try {
      await authService.logout(refreshToken);
    } catch {
      // Always clear local session even if server logout fails
    } finally {
      clearAuthState();
    }
  }, [clearAuthState]);

  const refreshUser = useCallback(async () => {
    const me = await authService.me();
    setUser(me);
    tokenStorage.updateUser(me);
    return me;
  }, []);

  const value = useMemo(() => {
    const roles = normalizeRoles(user?.roles || []);
    return {
      user,
      accessToken,
      roles,
      isAuthenticated: Boolean(accessToken && user),
      loading,
      bootstrapped,
      login,
      register,
      logout,
      refreshUser,
      clearAuthState,
    };
  }, [
    user,
    accessToken,
    loading,
    bootstrapped,
    login,
    register,
    logout,
    refreshUser,
    clearAuthState,
  ]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
