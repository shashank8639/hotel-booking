import { describe, it, expect, vi, beforeEach } from 'vitest';
import { Route, Routes } from 'react-router-dom';
import { screen } from '@testing-library/react';
import { RoleRoute } from '../../routes/RoleRoute';
import { renderWithProviders } from '../testUtils';

vi.mock('../../hooks/useAuth', () => ({
  useAuth: vi.fn(),
}));

import { useAuth } from '../../hooks/useAuth';

function Tree() {
  return (
    <Routes>
      <Route element={<RoleRoute allowedRoles={['ADMIN']} />}>
        <Route path="/admin" element={<div>Admin Area</div>} />
      </Route>
      <Route path="/unauthorized" element={<div>Unauthorized</div>} />
      <Route path="/login" element={<div>Login</div>} />
    </Routes>
  );
}

describe('RoleRoute', () => {
  beforeEach(() => vi.clearAllMocks());

  it('allows ADMIN role into protected outlet', () => {
    useAuth.mockReturnValue({ isAuthenticated: true, roles: ['ADMIN'] });
    renderWithProviders(<Tree />, { route: '/admin' });
    expect(screen.getByText('Admin Area')).toBeInTheDocument();
  });

  it('redirects CUSTOMER away from admin routes', () => {
    useAuth.mockReturnValue({ isAuthenticated: true, roles: ['CUSTOMER'] });
    renderWithProviders(<Tree />, { route: '/admin' });
    expect(screen.getByText('Unauthorized')).toBeInTheDocument();
  });
});
