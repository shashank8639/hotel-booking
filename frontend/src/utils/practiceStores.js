/**
 * Practice helpers — SEO document title + wishlist/compare storage sketches.
 * Full UI for wishlist/compare/map can build on these utilities.
 */
import { useEffect } from 'react';

/** Sets document.title (and optional description meta) for a page. */
export function usePageMeta({ title, description }) {
  useEffect(() => {
    const previous = document.title;
    if (title) {
      document.title = title;
    }
    let meta = document.querySelector('meta[name="description"]');
    const created = !meta;
    if (!meta) {
      meta = document.createElement('meta');
      meta.name = 'description';
      document.head.appendChild(meta);
    }
    const previousDescription = meta.content;
    if (description) {
      meta.content = description;
    }
    return () => {
      document.title = previous;
      if (description) {
        meta.content = previousDescription;
      }
      if (created) {
        meta.remove();
      }
    };
  }, [title, description]);
}

const WISHLIST_KEY = 'hb_wishlist_room_ids';
const COMPARE_KEY = 'hb_compare_room_ids';

function readIds(key) {
  try {
    return JSON.parse(localStorage.getItem(key) || '[]');
  } catch {
    return [];
  }
}

function writeIds(key, ids) {
  localStorage.setItem(key, JSON.stringify([...new Set(ids)]));
}

export const wishlistStore = {
  list: () => readIds(WISHLIST_KEY).map(Number),
  toggle(roomId) {
    const id = Number(roomId);
    const current = readIds(WISHLIST_KEY).map(Number);
    const next = current.includes(id) ? current.filter((x) => x !== id) : [...current, id];
    writeIds(WISHLIST_KEY, next);
    return next;
  },
  has(roomId) {
    return readIds(WISHLIST_KEY).map(Number).includes(Number(roomId));
  },
};

/** Compare is capped at 2 rooms for a side-by-side UI. */
export const compareStore = {
  list: () => readIds(COMPARE_KEY).map(Number).slice(0, 2),
  add(roomId) {
    const id = Number(roomId);
    let current = readIds(COMPARE_KEY).map(Number).filter((x) => x !== id);
    current.push(id);
    if (current.length > 2) {
      current = current.slice(-2);
    }
    writeIds(COMPARE_KEY, current);
    return current;
  },
  clear() {
    writeIds(COMPARE_KEY, []);
  },
};

/** Multi-room cart for one booking (Challenge). */
const CART_KEY = 'hb_booking_cart';

export const bookingCart = {
  get() {
    try {
      return JSON.parse(localStorage.getItem(CART_KEY) || '{"roomIds":[],"checkIn":"","checkOut":""}');
    } catch {
      return { roomIds: [], checkIn: '', checkOut: '' };
    }
  },
  save(cart) {
    localStorage.setItem(CART_KEY, JSON.stringify(cart));
  },
  addRoom(roomId, checkIn, checkOut) {
    const cart = this.get();
    const id = Number(roomId);
    if (!cart.roomIds.includes(id)) {
      cart.roomIds.push(id);
    }
    if (checkIn) cart.checkIn = checkIn;
    if (checkOut) cart.checkOut = checkOut;
    this.save(cart);
    return cart;
  },
  removeRoom(roomId) {
    const cart = this.get();
    cart.roomIds = cart.roomIds.filter((id) => id !== Number(roomId));
    this.save(cart);
    return cart;
  },
  clear() {
    localStorage.removeItem(CART_KEY);
  },
};
