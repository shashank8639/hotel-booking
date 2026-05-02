import { useMemo } from 'react';
import { calculateBookingPrice } from '../utils/priceCalculation';

/** Memoized price breakdown for selected room(s) + dates. */
export function useBookingPrice({ room, rooms, checkIn, checkOut }) {
  return useMemo(() => {
    const selected = rooms?.length ? rooms : room ? [room] : [];
    if (!selected.length) {
      return calculateBookingPrice({
        pricePerNight: 0,
        effectivePrice: 0,
        checkIn,
        checkOut,
      });
    }
    return calculateBookingPrice({
      rooms: selected,
      checkIn,
      checkOut,
    });
  }, [room, rooms, checkIn, checkOut]);
}
