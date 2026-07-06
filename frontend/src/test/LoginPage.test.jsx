import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material';
import LoginPage from '../pages/LoginPage';

vi.mock('../hooks/useAuth', () => ({
  useAuth: vi.fn(),
}));

import { useAuth } from '../hooks/useAuth';

function renderLogin(auth) {
  useAuth.mockReturnValue(auth);
  return render(
    <ThemeProvider theme={createTheme()}>
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>
    </ThemeProvider>
  );
}

describe('LoginPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('shows validation errors when empty', async () => {
    renderLogin({
      isAuthenticated: false,
      login: vi.fn(),
    });
    fireEvent.click(screen.getByRole('button', { name: /login/i }));
    expect(await screen.findByText(/email is required/i)).toBeInTheDocument();
    expect(screen.getByText(/password is required/i)).toBeInTheDocument();
  });

  it('calls login with credentials', async () => {
    const login = vi.fn().mockResolvedValue({ redirectTo: '/customer/dashboard' });
    renderLogin({ isAuthenticated: false, login });

    fireEvent.change(screen.getByRole('textbox', { name: /email/i }), {
      target: { value: 'a@b.com' },
    });
    fireEvent.change(document.querySelector('#login-password'), {
      target: { value: 'secret123' },
    });
    fireEvent.click(screen.getByRole('button', { name: /login/i }));

    await waitFor(() => {
      expect(login).toHaveBeenCalledWith({
        email: 'a@b.com',
        password: 'secret123',
        rememberMe: false,
      });
    });
  });
});
