import { nightsBetween, todayIso } from './format';

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const PHONE_RE = /^\+?[0-9\s\-()]{7,20}$/;

/**
 * Validates booking step fields (dates, guest count, room).
 * Returns a map of field → message (empty object = valid).
 */
export function validateBookingForm({
  checkIn,
  checkOut,
  guests,
  roomId,
  roomIds,
  capacity,
}) {
  const errors = {};
  const today = todayIso();
  const hasRooms = (roomIds && roomIds.length > 0) || roomId;

  if (!hasRooms) {
    errors.roomId = 'Please select a room';
  }
  if (!checkIn) {
    errors.checkIn = 'Check-in date is required';
  } else if (checkIn < today) {
    errors.checkIn = 'Check-in cannot be in the past';
  }
  if (!checkOut) {
    errors.checkOut = 'Check-out date is required';
  } else if (checkIn && checkOut <= checkIn) {
    errors.checkOut = 'Check-out must be after check-in';
  }
  const nights = nightsBetween(checkIn, checkOut);
  if (checkIn && checkOut && nights < 1) {
    errors.checkOut = 'Stay must be at least 1 night';
  }
  const guestCount = Number(guests);
  if (!guestCount || guestCount < 1) {
    errors.guests = 'At least 1 guest is required';
  } else if (capacity && guestCount > capacity) {
    errors.guests = `This room sleeps up to ${capacity} guests`;
  }

  return errors;
}

/**
 * Validates guest profile fields before booking create.
 */
export function validateGuestForm({ firstName, lastName, email, phone, address }) {
  const errors = {};

  if (!firstName?.trim()) {
    errors.firstName = 'First name is required';
  } else if (firstName.trim().length > 100) {
    errors.firstName = 'First name is too long';
  }

  if (!lastName?.trim()) {
    errors.lastName = 'Last name is required';
  } else if (lastName.trim().length > 100) {
    errors.lastName = 'Last name is too long';
  }

  if (!email?.trim()) {
    errors.email = 'Email is required';
  } else if (!EMAIL_RE.test(email.trim())) {
    errors.email = 'Enter a valid email address';
  }

  if (!phone?.trim()) {
    errors.phone = 'Phone number is required';
  } else if (!PHONE_RE.test(phone.trim())) {
    errors.phone = 'Enter a valid phone number';
  }

  if (address && address.length > 500) {
    errors.address = 'Address must be under 500 characters';
  }

  return errors;
}

export function isEmptyErrors(errors) {
  return !errors || Object.keys(errors).length === 0;
}
