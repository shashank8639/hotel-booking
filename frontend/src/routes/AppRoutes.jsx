import { lazy, Suspense } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import { Box, CircularProgress } from '@mui/material';
import { AuthLayout, MainLayout } from '../layouts/AuthLayout';
import AdminLayout from '../layouts/AdminLayout';
import { PrivateRoute } from './PrivateRoute';
import { RoleRoute } from './RoleRoute';
import { Roles } from '../auth/roles';
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';
import LandingPage from '../pages/LandingPage';
import HotelsPage from '../pages/HotelsPage';
import HotelDetailsPage from '../pages/HotelDetailsPage';
import RoomsPage from '../pages/RoomsPage';
import RoomDetailsPage from '../pages/RoomDetailsPage';
import BookingPage from '../pages/BookingPage';
import UnauthorizedPage from '../pages/UnauthorizedPage';
import NotFoundPage from '../pages/NotFoundPage';
import { CustomerDashboardPage, ReceptionDashboardPage } from '../pages/DashboardPages';

const CheckoutPage = lazy(() => import('../pages/CheckoutPage'));
const BookingPaymentPage = lazy(() => import('../pages/booking/BookingPaymentPage'));
const BookingSuccessPage = lazy(() => import('../pages/booking/BookingSuccessPage'));
const BookingFailurePage = lazy(() => import('../pages/booking/BookingFailurePage'));

const AdminDashboardHomePage = lazy(() => import('../pages/admin/AdminDashboardHomePage'));
const AdminRoomsPage = lazy(() => import('../pages/admin/AdminRoomsPage'));
const AdminGuestsPage = lazy(() => import('../pages/admin/AdminGuestsPage'));
const AdminBookingsPage = lazy(() => import('../pages/admin/AdminBookingsPage'));
const AdminPaymentsPage = lazy(() => import('../pages/admin/AdminPaymentsPage'));
const AdminReportsPage = lazy(() => import('../pages/admin/AdminReportsPage'));

function RouteFallback() {
  return (
    <Box sx={{ py: 8, display: 'flex', justifyContent: 'center' }}>
      <CircularProgress aria-label="Loading page" />
    </Box>
  );
}

/**
 * Public website + booking wizard + admin portal (lazy) + role dashboards.
 */
export default function AppRoutes() {
  return (
    <Suspense fallback={<RouteFallback />}>
      <Routes>
        <Route element={<AuthLayout />}>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
        </Route>

        <Route element={<MainLayout />}>
          <Route path="/" element={<LandingPage />} />
          <Route path="/hotels" element={<HotelsPage />} />
          <Route path="/hotels/:slug" element={<HotelDetailsPage />} />
          <Route path="/rooms" element={<RoomsPage />} />
          <Route path="/rooms/:id" element={<RoomDetailsPage />} />
          <Route path="/unauthorized" element={<UnauthorizedPage />} />

          <Route element={<PrivateRoute />}>
            <Route path="/book" element={<BookingPage />} />
            <Route path="/book/payment/:bookingId" element={<BookingPaymentPage />} />
            <Route path="/book/success/:bookingId" element={<BookingSuccessPage />} />
            <Route path="/book/failure/:bookingId" element={<BookingFailurePage />} />
            <Route path="/checkout/:bookingId" element={<CheckoutPage />} />

            <Route element={<RoleRoute allowedRoles={[Roles.CUSTOMER, Roles.ADMIN]} />}>
              <Route path="/customer/dashboard" element={<CustomerDashboardPage />} />
            </Route>

            <Route element={<RoleRoute allowedRoles={[Roles.RECEPTIONIST, Roles.ADMIN]} />}>
              <Route path="/reception/dashboard" element={<ReceptionDashboardPage />} />
            </Route>
          </Route>
        </Route>

        {/* Admin portal — own chrome (sidebar), not MainLayout navbar */}
        <Route element={<PrivateRoute />}>
          <Route element={<RoleRoute allowedRoles={[Roles.ADMIN]} />}>
            <Route path="/admin" element={<AdminLayout />}>
              <Route index element={<Navigate to="dashboard" replace />} />
              <Route path="dashboard" element={<AdminDashboardHomePage />} />
              <Route path="rooms" element={<AdminRoomsPage />} />
              <Route path="guests" element={<AdminGuestsPage />} />
              <Route path="bookings" element={<AdminBookingsPage />} />
              <Route path="payments" element={<AdminPaymentsPage />} />
              <Route path="reports" element={<AdminReportsPage />} />
            </Route>
          </Route>
        </Route>

        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </Suspense>
  );
}
