/**
 * Debounce a changing value — updates `debounced` only after `delayMs` of quiet.
 * Used so price filter typing doesn't refetch on every keystroke.
 */
import { useEffect, useState } from 'react';

export function useDebouncedValue(value, delayMs = 400) {
  const [debounced, setDebounced] = useState(value);

  useEffect(() => {
    const id = setTimeout(() => setDebounced(value), delayMs);
    return () => clearTimeout(id);
  }, [value, delayMs]);

  return debounced;
}
