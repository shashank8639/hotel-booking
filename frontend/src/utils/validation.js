/**
 * Client-side form validation helpers (complement Bean Validation on the API).
 */

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function isValidEmail(email) {
  return EMAIL_REGEX.test(String(email || '').trim());
}

export function getPasswordStrength(password = '') {
  let score = 0;
  if (password.length >= 8) score += 1;
  if (password.length >= 12) score += 1;
  if (/[A-Z]/.test(password)) score += 1;
  if (/[a-z]/.test(password)) score += 1;
  if (/[0-9]/.test(password)) score += 1;
  if (/[^A-Za-z0-9]/.test(password)) score += 1;

  if (score <= 2) {
    return { score, label: 'Weak', color: 'error' };
  }
  if (score <= 4) {
    return { score, label: 'Medium', color: 'warning' };
  }
  return { score, label: 'Strong', color: 'success' };
}

export function validateLoginForm({ email, password }) {
  const errors = {};
  if (!email?.trim()) {
    errors.email = 'Email is required';
  } else if (!isValidEmail(email)) {
    errors.email = 'Enter a valid email address';
  }
  if (!password) {
    errors.password = 'Password is required';
  }
  return errors;
}

export function validateRegisterForm({ firstName, lastName, email, password, confirmPassword }) {
  const errors = {};
  if (!firstName?.trim()) {
    errors.firstName = 'First name is required';
  }
  if (!lastName?.trim()) {
    errors.lastName = 'Last name is required';
  }
  if (!email?.trim()) {
    errors.email = 'Email is required';
  } else if (!isValidEmail(email)) {
    errors.email = 'Enter a valid email address';
  }
  if (!password) {
    errors.password = 'Password is required';
  } else if (password.length < 8) {
    errors.password = 'Password must be at least 8 characters';
  }
  if (!confirmPassword) {
    errors.confirmPassword = 'Confirm your password';
  } else if (password !== confirmPassword) {
    errors.confirmPassword = 'Passwords do not match';
  }
  return errors;
}
