import { createContext, useCallback, useEffect, useMemo, useReducer } from 'react';
import {
  clearWizardDraft,
  loadWizardDraft,
  saveWizardDraft,
} from '../utils/wizardDraftStorage';

/**
 * Holds multi-step booking draft across Room → Guest → Summary.
 * Draft is mirrored to sessionStorage; payment/success use server booking id.
 */
const BookingWizardContext = createContext(null);

const emptyGuest = {
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  address: '',
};

export const initialState = {
  step: 0,
  roomId: null,
  roomIds: [],
  room: null,
  rooms: [],
  checkIn: '',
  checkOut: '',
  guests: 2,
  specialRequests: '',
  guest: { ...emptyGuest },
  guestRecord: null,
  booking: null,
  availabilityNote: '',
  availabilityOk: null,
};

function reducer(state, action) {
  switch (action.type) {
    case 'HYDRATE_FROM_SEARCH': {
      const primaryId = action.payload.roomId;
      const fromDraft = action.payload.draft || {};
      const roomIds =
        primaryId != null
          ? Array.from(
              new Set([
                Number(primaryId),
                ...(fromDraft.roomIds || []).map(Number).filter((id) => id !== Number(primaryId)),
              ])
            ).slice(0, 2)
          : (fromDraft.roomIds || []).slice(0, 2);

      return {
        ...state,
        roomId: primaryId ?? fromDraft.roomIds?.[0] ?? state.roomId,
        roomIds: roomIds.length ? roomIds : state.roomIds,
        checkIn: action.payload.checkIn || fromDraft.checkIn || state.checkIn,
        checkOut: action.payload.checkOut || fromDraft.checkOut || state.checkOut,
        guests: action.payload.guests ?? fromDraft.guests ?? state.guests,
        specialRequests: fromDraft.specialRequests ?? state.specialRequests,
        guest: { ...emptyGuest, ...fromDraft.guest, ...state.guest },
        step: typeof fromDraft.step === 'number' ? fromDraft.step : 0,
      };
    }
    case 'SET_ROOM': {
      const room = action.payload;
      if (!room) return { ...state, room: null };
      const roomIds = state.roomIds.includes(room.id)
        ? state.roomIds
        : [room.id, ...state.roomIds.filter((id) => id !== room.id)].slice(0, 2);
      const rooms = mergeRooms(state.rooms, [room]);
      return {
        ...state,
        room,
        roomId: room.id,
        roomIds,
        rooms: rooms.filter((r) => roomIds.includes(r.id)),
      };
    }
    case 'SET_ROOMS': {
      const rooms = action.payload || [];
      const roomIds = rooms.map((r) => r.id).slice(0, 2);
      return {
        ...state,
        rooms,
        roomIds,
        roomId: roomIds[0] ?? null,
        room: rooms[0] ?? null,
      };
    }
    case 'SET_BOOKING_FIELDS':
      return { ...state, ...action.payload };
    case 'SET_GUEST_FIELDS':
      return { ...state, guest: { ...state.guest, ...action.payload } };
    case 'SET_GUEST_RECORD':
      return { ...state, guestRecord: action.payload };
    case 'SET_BOOKING':
      return { ...state, booking: action.payload };
    case 'SET_AVAILABILITY':
      return {
        ...state,
        availabilityNote: action.payload.note,
        availabilityOk: action.payload.ok,
      };
    case 'SET_STEP':
      return { ...state, step: action.payload };
    case 'NEXT':
      return { ...state, step: Math.min(state.step + 1, 4) };
    case 'BACK':
      return { ...state, step: Math.max(state.step - 1, 0) };
    case 'RESET':
      clearWizardDraft();
      return { ...initialState };
    default:
      return state;
  }
}

function mergeRooms(existing, incoming) {
  const map = new Map((existing || []).map((r) => [r.id, r]));
  (incoming || []).forEach((r) => map.set(r.id, r));
  return [...map.values()];
}

export function BookingWizardProvider({ children, initial }) {
  const [state, dispatch] = useReducer(reducer, {
    ...initialState,
    ...loadWizardDraft(),
    ...initial,
  });

  useEffect(() => {
    saveWizardDraft(state);
  }, [state]);

  const hydrateFromSearch = useCallback((payload) => {
    dispatch({
      type: 'HYDRATE_FROM_SEARCH',
      payload: { ...payload, draft: loadWizardDraft() },
    });
  }, []);

  const setRoom = useCallback((room) => {
    dispatch({ type: 'SET_ROOM', payload: room });
  }, []);

  const setRooms = useCallback((rooms) => {
    dispatch({ type: 'SET_ROOMS', payload: rooms });
  }, []);

  const setBookingFields = useCallback((payload) => {
    dispatch({ type: 'SET_BOOKING_FIELDS', payload });
  }, []);

  const setGuestFields = useCallback((payload) => {
    dispatch({ type: 'SET_GUEST_FIELDS', payload });
  }, []);

  const setGuestRecord = useCallback((guestRecord) => {
    dispatch({ type: 'SET_GUEST_RECORD', payload: guestRecord });
  }, []);

  const setBooking = useCallback((booking) => {
    dispatch({ type: 'SET_BOOKING', payload: booking });
  }, []);

  const setAvailability = useCallback((note, ok = null) => {
    dispatch({ type: 'SET_AVAILABILITY', payload: { note, ok } });
  }, []);

  const setStep = useCallback((step) => {
    dispatch({ type: 'SET_STEP', payload: step });
  }, []);

  const next = useCallback(() => dispatch({ type: 'NEXT' }), []);
  const back = useCallback(() => dispatch({ type: 'BACK' }), []);
  const reset = useCallback(() => dispatch({ type: 'RESET' }), []);

  const totalCapacity = useMemo(
    () => (state.rooms || []).reduce((sum, r) => sum + (r.capacity || 0), 0),
    [state.rooms]
  );

  const value = useMemo(
    () => ({
      ...state,
      totalCapacity,
      hydrateFromSearch,
      setRoom,
      setRooms,
      setBookingFields,
      setGuestFields,
      setGuestRecord,
      setBooking,
      setAvailability,
      setStep,
      next,
      back,
      reset,
      clearDraft: clearWizardDraft,
    }),
    [
      state,
      totalCapacity,
      hydrateFromSearch,
      setRoom,
      setRooms,
      setBookingFields,
      setGuestFields,
      setGuestRecord,
      setBooking,
      setAvailability,
      setStep,
      next,
      back,
      reset,
    ]
  );

  return (
    <BookingWizardContext.Provider value={value}>{children}</BookingWizardContext.Provider>
  );
}

export default BookingWizardContext;
