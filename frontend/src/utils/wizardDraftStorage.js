/**
 * Persist booking wizard draft in sessionStorage so refresh / accidental tab close
 * does not wipe guest details mid-flow. Cleared after booking is created.
 */
export const WIZARD_DRAFT_KEY = 'hb_booking_wizard_draft';

const PERSIST_FIELDS = [
  'step',
  'roomIds',
  'checkIn',
  'checkOut',
  'guests',
  'specialRequests',
  'guest',
];

export function loadWizardDraft() {
  try {
    const raw = sessionStorage.getItem(WIZARD_DRAFT_KEY);
    if (!raw) return null;
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

export function saveWizardDraft(state) {
  if (typeof sessionStorage === 'undefined') return;
  const draft = {};
  PERSIST_FIELDS.forEach((key) => {
    if (state[key] !== undefined) draft[key] = state[key];
  });
  sessionStorage.setItem(WIZARD_DRAFT_KEY, JSON.stringify(draft));
}

export function clearWizardDraft() {
  if (typeof sessionStorage === 'undefined') return;
  sessionStorage.removeItem(WIZARD_DRAFT_KEY);
}
