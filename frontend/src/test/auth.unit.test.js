import { describe, it, expect, beforeEach } from 'vitest';
import { tokenStorage } from '../auth/tokenStorage';
import { getPasswordStrength, isValidEmail, validateLoginForm, validateRegisterForm } from '../utils/validation';
import { getDefaultHomePath, hasAnyRole, Roles } from '../auth/roles';

describe('tokenStorage', () => {
  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
  });

  it('stores session in sessionStorage by default', () => {
    tokenStorage.saveSession(
      { accessToken: 'a', refreshToken: 'r', user: { email: 'a@b.com', roles: ['CUSTOMER'] } },
      false
    );
    expect(sessionStorage.getItem('hb_access_token')).toBe('a');
    expect(localStorage.getItem('hb_access_token')).toBeNull();
    expect(tokenStorage.getUser().email).toBe('a@b.com');
  });

  it('stores session in localStorage when remember me is true', () => {
    tokenStorage.saveSession({ accessToken: 'a', refreshToken: 'r', user: { id: 1 } }, true);
    expect(localStorage.getItem('hb_access_token')).toBe('a');
    expect(tokenStorage.isRememberMe()).toBe(true);
  });

  it('clears tokens on clear()', () => {
    tokenStorage.saveSession({ accessToken: 'a', refreshToken: 'r', user: { id: 1 } }, true);
    tokenStorage.clear();
    expect(tokenStorage.getAccessToken()).toBeNull();
    expect(tokenStorage.getRefreshToken()).toBeNull();
  });
});

describe('validation', () => {
  it('validates email format', () => {
    expect(isValidEmail('user@hotel.com')).toBe(true);
    expect(isValidEmail('bad')).toBe(false);
  });

  it('requires login fields', () => {
    expect(validateLoginForm({ email: '', password: '' })).toEqual({
      email: 'Email is required',
      password: 'Password is required',
    });
  });

  it('checks confirm password', () => {
    const errors = validateRegisterForm({
      firstName: 'A',
      lastName: 'B',
      email: 'a@b.com',
      password: 'password1',
      confirmPassword: 'password2',
    });
    expect(errors.confirmPassword).toBe('Passwords do not match');
  });

  it('rates password strength', () => {
    expect(getPasswordStrength('abc').label).toBe('Weak');
    expect(getPasswordStrength('Abcdef1!xyz').label).toBe('Strong');
  });
});

describe('roles', () => {
  it('computes default home path', () => {
    expect(getDefaultHomePath(['ADMIN'])).toBe('/admin/dashboard');
    expect(getDefaultHomePath(['CUSTOMER'])).toBe('/customer/dashboard');
  });

  it('checks role membership', () => {
    expect(hasAnyRole(['ROLE_ADMIN'], [Roles.ADMIN])).toBe(true);
    expect(hasAnyRole(['CUSTOMER'], [Roles.ADMIN])).toBe(false);
  });
});
