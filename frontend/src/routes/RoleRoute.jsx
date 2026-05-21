import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { hasAnyRole } from '../auth/roles';

/**
 * Authorization guard: user must have at least one of the allowed roles.
 */
export function RoleRoute({ allowedRoles = [] }) {
  const { isAuthenticated, roles } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (!hasAnyRole(roles, allowedRoles)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return <Outlet />;
}
