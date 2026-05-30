import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, fireEvent, waitFor } from '@testing-library/react';
import RegisterPage from '../../pages/RegisterPage';
import { renderWithProviders } from '../testUtils';

vi.mock('../../hooks/useAuth', () => ({
  useAuth: vi.fn(),
}));

import { useAuth } from '../../hooks/useAuth';

describe('RegisterPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('shows validation errors when required fields are empty', async () => {
    useAuth.mockReturnValue({ register: vi.fn() });
    renderWithProviders(<RegisterPage />);

    fireEvent.click(screen.getByRole('button', { name: /register/i }));

    expect(await screen.findByText(/first name is required/i)).toBeInTheDocument();
  });

  it('calls register with form values on valid submit', async () => {
    const register = vi.fn().mockResolvedValue({ redirectTo: '/customer/dashboard' });
    useAuth.mockReturnValue({ register });
    renderWithProviders(<RegisterPage />);

    fireEvent.change(screen.getByLabelText(/first name/i), { target: { value: 'Rahul' } });
    fireEvent.change(screen.getByLabelText(/last name/i), { target: { value: 'Sharma' } });
    fireEvent.change(screen.getByLabelText(/^email/i), { target: { value: 'rahul@example.com' } });
    fireEvent.change(screen.getByLabelText(/^password/i), { target: { value: 'password123' } });
    fireEvent.change(screen.getByLabelText(/confirm password/i), { target: { value: 'password123' } });

    fireEvent.click(screen.getByRole('button', { name: /create account|register|sign up/i }));

    await waitFor(() => {
      expect(register).toHaveBeenCalled();
    });
    expect(register.mock.calls[0][0]).toMatchObject({
      firstName: 'Rahul',
      lastName: 'Sharma',
      email: 'rahul@example.com',
      password: 'password123',
    });
  });
});
