import { useContext } from 'react';
import BookingWizardContext from '../context/BookingWizardContext';

/** Access multi-step booking draft + actions. */
export function useBookingWizard() {
  const ctx = useContext(BookingWizardContext);
  if (!ctx) {
    throw new Error('useBookingWizard must be used within BookingWizardProvider');
  }
  return ctx;
}
