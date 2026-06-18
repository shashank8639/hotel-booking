import { useCallback, useEffect, useState } from 'react';

/**
 * Generic paginated Spring Page loader.
 * fetcher(filters) → { content, totalElements, totalPages, number, size }
 */
export function usePagedResource(fetcher, initialFilters = {}) {
  const [filters, setFilters] = useState({ page: 0, size: 10, ...initialFilters });
  const [data, setData] = useState({ content: [], totalElements: 0, totalPages: 0, number: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const page = await fetcher(filters);
      setData({
        content: page.content || [],
        totalElements: page.totalElements ?? 0,
        totalPages: page.totalPages ?? 0,
        number: page.number ?? filters.page ?? 0,
        size: page.size ?? filters.size ?? 10,
      });
    } catch (err) {
      setError(err.message || 'Failed to load data');
      setData({ content: [], totalElements: 0, totalPages: 0, number: 0 });
    } finally {
      setLoading(false);
    }
  }, [fetcher, filters]);

  useEffect(() => {
    load();
  }, [load]);

  const setPage = (page) => setFilters((f) => ({ ...f, page }));
  const setPageSize = (size) => setFilters((f) => ({ ...f, page: 0, size }));
  const patchFilters = (patch) => setFilters((f) => ({ ...f, page: 0, ...patch }));

  return {
    ...data,
    filters,
    loading,
    error,
    setPage,
    setPageSize,
    patchFilters,
    setFilters,
    reload: load,
  };
}
