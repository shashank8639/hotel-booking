import { useMemo } from 'react';
import { useSearchParams } from 'react-router-dom';
import {
  Box,
  Button,
  Container,
  Grid,
  Pagination,
  Stack,
  Typography,
} from '@mui/material';
import { SearchBar } from '../components/home/SearchBar';
import { RoomCard } from '../components/rooms/RoomCard';
import { RoomFilters } from '../components/rooms/RoomFilters';
import { RoomCardSkeleton } from '../components/common/LoadingSkeletons';
import { EmptyState, ErrorState } from '../components/common/ErrorState';
import { useRoomSearch } from '../hooks/useRoomSearch';
import { parseSearchParams, toSearchParams, validateSearchState } from '../utils/searchParams';

/**
 * Room search results — filters, sorting, pagination against /rooms/search.
 */
export default function RoomsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const filters = useMemo(() => parseSearchParams(searchParams), [searchParams]);
  const filterErrors = validateSearchState(filters);
  const { data, loading, error, reload } = useRoomSearch(filters);

  const updateFilters = (next) => {
    setSearchParams(toSearchParams(next));
  };

  return (
    <Container maxWidth="lg" sx={{ py: 3 }}>
      <Typography variant="h4" fontWeight={750} gutterBottom>
        Search rooms
      </Typography>
      <Typography color="text.secondary" sx={{ mb: 2 }}>
        Single-hotel inventory · results update from the Spring search API.
      </Typography>

      <Box sx={{ mb: 3 }}>
        <SearchBar initialValues={filters} compact />
      </Box>

      {Object.keys(filterErrors).length > 0 && (
        <ErrorState message={Object.values(filterErrors).join(' · ')} />
      )}

      <Grid container spacing={3}>
        <Grid item xs={12} md={3}>
          <RoomFilters value={filters} onChange={updateFilters} />
        </Grid>
        <Grid item xs={12} md={9}>
          {loading && <RoomCardSkeleton />}
          {error && <ErrorState message={error} onRetry={reload} />}
          {!loading && !error && data?.content?.length === 0 && (
            <EmptyState
              title="No rooms match"
              body="Try widening price range, clearing room type, or picking different dates."
              action={
                <Button
                  variant="outlined"
                  onClick={() =>
                    updateFilters({
                      ...filters,
                      roomType: '',
                      minPrice: '',
                      maxPrice: '',
                      status: 'AVAILABLE',
                      page: 0,
                    })
                  }
                >
                  Clear filters
                </Button>
              }
            />
          )}
          {!loading && !error && data?.content?.length > 0 && (
            <Stack spacing={2}>
              <Typography variant="body2" color="text.secondary">
                Showing {data.numberOfElements} of {data.totalElements} rooms
              </Typography>
              <Grid container spacing={2}>
                {data.content.map((room) => (
                  <Grid item xs={12} sm={6} key={room.id}>
                    <RoomCard
                      room={room}
                      checkIn={filters.checkIn}
                      checkOut={filters.checkOut}
                      guests={filters.guests}
                    />
                  </Grid>
                ))}
              </Grid>
              {data.totalPages > 1 && (
                <Pagination
                  color="primary"
                  page={(filters.page || 0) + 1}
                  count={data.totalPages}
                  onChange={(_, page) => updateFilters({ ...filters, page: page - 1 })}
                  sx={{ alignSelf: 'center', pt: 2 }}
                />
              )}
            </Stack>
          )}
        </Grid>
      </Grid>
    </Container>
  );
}
