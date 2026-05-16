import { addDaysIso, todayIso } from './format';

/**
 * Search state ↔ URL query string helpers (shareable / bookmarkable searches).
 */
export function defaultSearchState() {
  const checkIn = todayIso();
  return {
    location: 'Mumbai',
    checkIn,
    checkOut: addDaysIso(checkIn, 2),
    guests: 2,
    roomType: '',
    minPrice: '',
    maxPrice: '',
    status: 'AVAILABLE',
    sort: 'pricePerNight,asc',
    page: 0,
    size: 6,
  };
}

export function parseSearchParams(searchParams) {
  const defaults = defaultSearchState();
  return {
    location: searchParams.get('location') || defaults.location,
    checkIn: searchParams.get('checkIn') || defaults.checkIn,
    checkOut: searchParams.get('checkOut') || defaults.checkOut,
    guests: Number(searchParams.get('guests') || defaults.guests),
    roomType: searchParams.get('roomType') || '',
    minPrice: searchParams.get('minPrice') || '',
    maxPrice: searchParams.get('maxPrice') || '',
    status: searchParams.get('status') || 'AVAILABLE',
    sort: searchParams.get('sort') || defaults.sort,
    page: Number(searchParams.get('page') || 0),
    size: Number(searchParams.get('size') || defaults.size),
  };
}

export function toSearchParams(state) {
  const params = new URLSearchParams();
  Object.entries(state).forEach(([key, value]) => {
    if (value === '' || value === null || value === undefined) return;
    params.set(key, String(value));
  });
  return params;
}

export function validateSearchState(state) {
  const errors = {};
  if (!state.checkIn) errors.checkIn = 'Check-in is required';
  if (!state.checkOut) errors.checkOut = 'Check-out is required';
  if (state.checkIn && state.checkOut && state.checkOut <= state.checkIn) {
    errors.checkOut = 'Check-out must be after check-in';
  }
  if (!state.guests || state.guests < 1) {
    errors.guests = 'At least 1 guest required';
  }
  if (state.minPrice !== '' && state.maxPrice !== '' && Number(state.minPrice) > Number(state.maxPrice)) {
    errors.maxPrice = 'Max price must be ≥ min price';
  }
  return errors;
}
