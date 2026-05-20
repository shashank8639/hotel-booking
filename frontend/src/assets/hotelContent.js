/**
 * Platform branding (StayFinder) + shared marketing content for Module 16+.
 * Individual hotel names/addresses come from the API; this file is site chrome only.
 */
export const PLATFORM = {
  name: 'StayFinder',
  tagline: 'Find stays across Telangana — compare hotels, rooms, and rates',
  location: 'Telangana, India',
  address: 'Hyderabad · Warangal · and cities across Telangana',
  phone: '+91 40 4000 1600',
  email: 'hello@stayfinder.example',
  description:
    'StayFinder is a multi-hotel booking platform. Search by city, compare properties, and book rooms with the same secure checkout pattern used in enterprise travel apps.',
};

/** @deprecated Use PLATFORM — kept so older single-hotel UI paths still compile. */
export const HOTEL = PLATFORM;

export const DESTINATIONS = [
  { id: 'hyderabad', name: 'Hyderabad', blurb: 'City stays & tech corridor', image: 'https://images.unsplash.com/photo-1562979314-bee745d16375?w=800&q=80' },
  { id: 'warangal', name: 'Warangal', blurb: 'Heritage & weekend trips', image: 'https://images.unsplash.com/photo-1477587458883-471835f57e5c?w=800&q=80' },
  { id: 'karimnagar', name: 'Karimnagar', blurb: 'Regional business stays', image: 'https://images.unsplash.com/photo-1587474260584-136574528ed5?w=800&q=80' },
  { id: 'nizamabad', name: 'Nizamabad', blurb: 'North Telangana stops', image: 'https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?w=800&q=80' },
];

export const SERVICES = [
  { title: 'Multi-hotel search', body: 'Filter by city, stars, category, and price — same discovery pattern OTAs use.' },
  { title: 'Flexible stays', body: 'Clear rates, transparent taxes, and date changes when inventory allows.' },
  { title: 'Secure checkout', body: 'JWT-secured booking APIs with Razorpay-ready payment flow.' },
  { title: 'Owner & admin ready', body: 'Hotel owners manage inventory; admins approve listings across the catalog.' },
];

export const TESTIMONIALS = [
  { name: 'Ananya R.', quote: 'Found a Hyderabad stay in minutes and the room matched the photos.', role: 'Weekend guest' },
  { name: 'Vikram S.', quote: 'Ideal for work trips — quiet properties and reliable Wi-Fi near HITEC.', role: 'Business traveler' },
  { name: 'Meera K.', quote: 'Loved comparing Warangal and Hyderabad options in one search.', role: 'Family stay' },
];

export const AMENITIES_BY_TYPE = {
  STANDARD: ['Wi-Fi', 'AC', 'TV', 'Workspace'],
  DELUXE: ['Wi-Fi', 'AC', 'TV', 'Mini bar', 'City view'],
  EXECUTIVE: ['Wi-Fi', 'AC', 'Lounge access', 'Workspace', 'Rain shower'],
  SUITE: ['Wi-Fi', 'Living area', 'Bathtub', 'Ocean view', 'Butler request'],
  FAMILY: ['Wi-Fi', 'Twin beds option', 'Kids amenities', 'AC', 'TV'],
  PRESIDENTIAL: ['Private lounge', 'Butler', 'Jacuzzi', 'Panoramic view', 'Dining area'],
};

export const DEFAULT_AMENITIES = ['Wi-Fi', 'AC', 'TV', 'Housekeeping'];

export const ROOM_TYPE_FALLBACK_IMAGES = {
  STANDARD: 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=1200&q=80',
  DELUXE: 'https://images.unsplash.com/photo-1618773928121-c32242e63f39?w=1200&q=80',
  EXECUTIVE: 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=1200&q=80',
  SUITE: 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=1200&q=80',
  FAMILY: 'https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=1200&q=80',
  PRESIDENTIAL: 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=1200&q=80',
};

export function amenitiesForRoomType(roomType) {
  return AMENITIES_BY_TYPE[roomType] || DEFAULT_AMENITIES;
}

export function primaryImageForRoom(room) {
  const primary = room?.images?.find((img) => img.primary) || room?.images?.[0];
  if (primary?.imageUrl) {
    return primary.imageUrl;
  }
  return ROOM_TYPE_FALLBACK_IMAGES[room?.roomType] || ROOM_TYPE_FALLBACK_IMAGES.STANDARD;
}

/** Illustrative rating for UI when API rating is absent. */
export function displayRatingForRoomType(roomType) {
  const map = {
    STANDARD: 4.2,
    DELUXE: 4.5,
    EXECUTIVE: 4.6,
    SUITE: 4.8,
    FAMILY: 4.4,
    PRESIDENTIAL: 4.9,
  };
  return map[roomType] || 4.3;
}
