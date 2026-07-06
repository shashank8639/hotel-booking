import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import { Route, Routes } from 'react-router-dom';
import { PrivateRoute } from '../../routes/PrivateRoute';
import { renderWithProviders } from '../testUtils';

vi.mock('../../hooks/useAuth', () => ({
  useAuth: vi.fn(),
}));

import { useAuth } from '../../hooks/useAuth';

/**
 * Practice #4 — unauthenticated users must be sent to /login.
 */
describe('PrivateRoute (practice)', () => {
  beforeEach(() => vi.clearAllMocks());

  it('redirects unauthenticated users to /login', async () => {
    useAuth.mockReturnValue({
      isAuthenticated: false,
      loading: false,
      bootstrapped: true,
    });

    renderWithProviders(
      <Routes>
        <Route path="/login" element={<div>Login Page</div>} />
        <Route element={<PrivateRoute />}>
          <Route path="/customer/dashboard" element={<div>Customer Home</div>} />
        </Route>
      </Routes>,
      { route: '/customer/dashboard' }
    );

    await waitFor(() => {
      expect(screen.getByText('Login Page')).toBeInTheDocument();
    });
    expect(screen.queryByText('Customer Home')).not.toBeInTheDocument();
  });
});
