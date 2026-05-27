import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, act } from '@testing-library/react';
import { AuthProvider } from '../context/AuthContext';
import { useAuth } from '../hooks/useAuth';

vi.mock('../services/authService', () => ({
  authService: {
    me: vi.fn(),
    login: vi.fn(),
    register: vi.fn(),
    logout: vi.fn(),
    refresh: vi.fn(),
  },
}));

vi.mock('../services/api', () => ({
  registerUnauthorizedHandler: vi.fn(),
}));

import { authService } from '../services/authService';
import { tokenStorage } from '../auth/tokenStorage';

function Probe() {
  const auth = useAuth();
  return (
    <div>
      <span data-testid="auth">{String(auth.isAuthenticated)}</span>
      <span data-testid="email">{auth.user?.email || ''}</span>
      <button type="button" onClick={() => auth.logout()}>
        Logout
      </button>
    </div>
  );
}

describe('AuthContext', () => {
  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
    vi.clearAllMocks();
  });

  it('bootstraps as logged out when no token', async () => {
    authService.me.mockRejectedValue(new Error('nope'));
    render(
      <AuthProvider>
        <Probe />
      </AuthProvider>
    );
    await waitFor(() => {
      expect(screen.getByTestId('auth').textContent).toBe('false');
    });
  });

  it('restores user from /auth/me when token exists', async () => {
    tokenStorage.saveSession(
      {
        accessToken: 'access',
        refreshToken: 'refresh',
        user: { email: 'old@hotel.com', roles: ['CUSTOMER'] },
      },
      true
    );
    authService.me.mockResolvedValue({
      id: 1,
      email: 'new@hotel.com',
      firstName: 'A',
      lastName: 'B',
      roles: ['CUSTOMER'],
    });

    render(
      <AuthProvider>
        <Probe />
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId('auth').textContent).toBe('true');
      expect(screen.getByTestId('email').textContent).toBe('new@hotel.com');
    });
  });

  it('logout clears session', async () => {
    tokenStorage.saveSession(
      { accessToken: 'access', refreshToken: 'refresh', user: { email: 'a@b.com', roles: ['CUSTOMER'] } },
      false
    );
    authService.me.mockResolvedValue({ email: 'a@b.com', roles: ['CUSTOMER'] });
    authService.logout.mockResolvedValue(undefined);

    render(
      <AuthProvider>
        <Probe />
      </AuthProvider>
    );

    await waitFor(() => expect(screen.getByTestId('auth').textContent).toBe('true'));

    await act(async () => {
      screen.getByText('Logout').click();
    });

    await waitFor(() => {
      expect(screen.getByTestId('auth').textContent).toBe('false');
      expect(tokenStorage.getAccessToken()).toBeNull();
    });
  });
});
