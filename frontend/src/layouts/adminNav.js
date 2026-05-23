import DashboardIcon from '@mui/icons-material/Dashboard';
import MeetingRoomIcon from '@mui/icons-material/MeetingRoom';
import PeopleIcon from '@mui/icons-material/People';
import EventNoteIcon from '@mui/icons-material/EventNote';
import PaymentsIcon from '@mui/icons-material/Payments';
import AssessmentIcon from '@mui/icons-material/Assessment';

/** Sidebar items for AdminLayout. */
export const ADMIN_NAV = [
  { label: 'Dashboard', path: '/admin/dashboard', icon: DashboardIcon },
  { label: 'Rooms', path: '/admin/rooms', icon: MeetingRoomIcon },
  { label: 'Guests', path: '/admin/guests', icon: PeopleIcon },
  { label: 'Bookings', path: '/admin/bookings', icon: EventNoteIcon },
  { label: 'Payments', path: '/admin/payments', icon: PaymentsIcon },
  { label: 'Reports', path: '/admin/reports', icon: AssessmentIcon },
];
