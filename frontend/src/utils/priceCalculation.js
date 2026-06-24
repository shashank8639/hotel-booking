import { nightsBetween } from './format';

/** Display GST rate used for booking summary estimates (UI only). */
export const TAX_RATE = 0.12;

/** Display service charge rate (UI only). */
export const SERVICE_RATE = 0.05;

/**
 * Client-side price breakdown for one or more rooms.
 * Authoritative amount still comes from Booking Engine after create.
 */
export function calculateBookingPrice({
  pricePerNight,
  effectivePrice,
  checkIn,
  checkOut,
  roomCount = 1,
  rooms,
}) {
  const nights = nightsBetween(checkIn, checkOut);

  if (Array.isArray(rooms) && rooms.length > 0) {
    return calculateMultiRoomPrice({ rooms, checkIn, checkOut });
  }

  const listRate = Number(pricePerNight ?? 0);
  const paidRate = Number(effectivePrice ?? pricePerNight ?? 0);
  const roomCharges = paidRate * nights * roomCount;
  const listCharges = listRate * nights * roomCount;
  const discount = Math.max(listCharges - roomCharges, 0);
  const taxes = roundMoney(roomCharges * TAX_RATE);
  const serviceCharges = roundMoney(roomCharges * SERVICE_RATE);
  const grandTotal = roundMoney(roomCharges + taxes + serviceCharges);

  return {
    nights,
    roomCount,
    ratePerNight: paidRate,
    listRatePerNight: listRate,
    roomCharges: roundMoney(roomCharges),
    discount: roundMoney(discount),
    taxes,
    serviceCharges,
    grandTotal,
    lines: [],
  };
}

export function calculateMultiRoomPrice({ rooms, checkIn, checkOut }) {
  const nights = nightsBetween(checkIn, checkOut);
  let roomCharges = 0;
  let listCharges = 0;
  const lines = (rooms || []).map((room) => {
    const listRate = Number(room.pricePerNight ?? 0);
    const paidRate = Number(room.effectivePrice ?? room.pricePerNight ?? 0);
    const subtotal = paidRate * nights;
    roomCharges += subtotal;
    listCharges += listRate * nights;
    return {
      roomId: room.id,
      label: `${room.roomType || 'Room'} ${room.roomNumber || room.id}`,
      ratePerNight: paidRate,
      subtotal: roundMoney(subtotal),
    };
  });

  const discount = Math.max(listCharges - roomCharges, 0);
  const taxes = roundMoney(roomCharges * TAX_RATE);
  const serviceCharges = roundMoney(roomCharges * SERVICE_RATE);
  const grandTotal = roundMoney(roomCharges + taxes + serviceCharges);
  const avgRate = rooms?.length ? roomCharges / Math.max(nights * rooms.length, 1) : 0;

  return {
    nights,
    roomCount: rooms?.length || 0,
    ratePerNight: roundMoney(avgRate),
    listRatePerNight: 0,
    roomCharges: roundMoney(roomCharges),
    discount: roundMoney(discount),
    taxes,
    serviceCharges,
    grandTotal,
    lines,
  };
}

export function roundMoney(value) {
  return Math.round((Number(value) + Number.EPSILON) * 100) / 100;
}
