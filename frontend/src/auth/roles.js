/**
 * Role names used for RBAC in the UI.
 * Backend currently seeds ADMIN and CUSTOMER (Module 3).
 * RECEPTIONIST is supported in the frontend for future backend roles.
 */
export const Roles = Object.freeze({
  ADMIN: 'ADMIN',
  CUSTOMER: 'CUSTOMER',
  RECEPTIONIST: 'RECEPTIONIST',
});

export const ROLE_HOME = Object.freeze({
  [Roles.ADMIN]: '/admin/dashboard',
  [Roles.RECEPTIONIST]: '/reception/dashboard',
  [Roles.CUSTOMER]: '/customer/dashboard',
});

export function normalizeRoles(roles = []) {
  return roles.map((role) => String(role).replace(/^ROLE_/, '').toUpperCase());
}

export function hasAnyRole(userRoles = [], allowedRoles = []) {
  if (!allowedRoles.length) {
    return true;
  }
  const normalized = normalizeRoles(userRoles);
  return allowedRoles.some((role) => normalized.includes(role));
}

export function getDefaultHomePath(userRoles = []) {
  const normalized = normalizeRoles(userRoles);
  if (normalized.includes(Roles.ADMIN)) {
    return ROLE_HOME[Roles.ADMIN];
  }
  if (normalized.includes(Roles.RECEPTIONIST)) {
    return ROLE_HOME[Roles.RECEPTIONIST];
  }
  if (normalized.includes(Roles.CUSTOMER)) {
    return ROLE_HOME[Roles.CUSTOMER];
  }
  return '/';
}
