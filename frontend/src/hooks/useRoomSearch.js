import { useCallback, useEffect, useState } from 'react';
import { roomService } from '../services/roomService';

/**
 * Loads a Spring Page of rooms for the public catalog / search screen.
 */
export function useRoomSearch(filters) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const reload = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const page = await roomService.search({
        roomType: filters.roomType || undefined,
        status: filters.status || 'AVAILABLE',
        minCapacity: filters.guests || undefined,
        minPrice: filters.minPrice === '' ? undefined : filters.minPrice,
        maxPrice: filters.maxPrice === '' ? undefined : filters.maxPrice,
        page: filters.page ?? 0,
        size: filters.size ?? 6,
        sort: filters.sort || 'pricePerNight,asc',
      });
      setData(page);
    } catch (err) {
      setError(err.message || 'Failed to load rooms');
      setData(null);
    } finally {
      setLoading(false);
    }
  }, [filters]);

  useEffect(() => {
    reload();
  }, [reload]);

  return { data, loading, error, reload };
}
