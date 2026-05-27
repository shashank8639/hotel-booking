import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { PrivateRoute } from '../routes/PrivateRoute';
import { RoleRoute } from '../routes/RoleRoute';
import { Roles } from '../auth/roles';

vi.mock('../hooks/useAuth', () => ({
  useAuth: vi.fn(),
}));

import { useAuth } from '../hooks/useAuth';

function renderWithRoutes(authValue, initialPath = '/secure') {
  useAuth.mockReturnValue(authValue);
  return render(
    <MemoryRouter initialEntries={[initialPath]}>
      <Routes>
        <Route path="/login" element={<div>Login Page</div>} />
        <Route path="/unauthorized" element={<div>Unauthorized Page</div>} />
        <Route element={<PrivateRoute />}>
          <Route element={<RoleRoute allowedRoles={[Roles.ADMIN]} />}>
            <Route path="/secure" element={<div>Secure Admin</div>} />
          </Route>
        </Route>
      </Routes>
    </MemoryRouter>
  );
}

describe('Protected routes', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('shows loading while bootstrapping', () => {
    renderWithRoutes({
      isAuthenticated: false,
      loading: true,
      bootstrapped: false,
      roles: [],
    });
    expect(screen.getByLabelText(/loading session/i)).toBeInTheDocument();
  });

  it('redirects unauthenticated users to login', async () => {
    renderWithRoutes({
      isAuthenticated: false,
      loading: false,
      bootstrapped: true,
      roles: [],
    });
    await waitFor(() => {
      expect(screen.getByText('Login Page')).toBeInTheDocument();
    });
  });

  it('allows admin into role route', async () => {
    renderWithRoutes({
      isAuthenticated: true,
      loading: false,
      bootstrapped: true,
      roles: ['ADMIN'],
    });
    await waitFor(() => {
      expect(screen.getByText('Secure Admin')).toBeInTheDocument();
    });
  });

  it('sends customer to unauthorized for admin route', async () => {
    renderWithRoutes({
      isAuthenticated: true,
      loading: false,
      bootstrapped: true,
      roles: ['CUSTOMER'],
    });
    await waitFor(() => {
      expect(screen.getByText('Unauthorized Page')).toBeInTheDocument();
    });
  });
});
