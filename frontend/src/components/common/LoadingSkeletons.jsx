import { Box, Skeleton, Stack, Card, CardContent, Grid } from '@mui/material';

/** Lightweight loading placeholders for room grids and detail pages. */
export function RoomCardSkeleton({ count = 6 }) {
  return (
    <Grid container spacing={2}>
      {Array.from({ length: count }).map((_, i) => (
        <Grid key={i} item xs={12} sm={6} md={4}>
          <Card>
            <Skeleton variant="rectangular" height={180} />
            <CardContent>
              <Skeleton width="60%" />
              <Skeleton width="40%" />
              <Skeleton width="80%" />
            </CardContent>
          </Card>
        </Grid>
      ))}
    </Grid>
  );
}

export function DetailSkeleton() {
  return (
    <Stack spacing={2}>
      <Skeleton variant="rectangular" height={360} sx={{ borderRadius: 2 }} />
      <Skeleton width="40%" height={36} />
      <Skeleton width="70%" />
      <Box sx={{ display: 'flex', gap: 1 }}>
        <Skeleton width={80} height={32} />
        <Skeleton width={80} height={32} />
        <Skeleton width={80} height={32} />
      </Box>
    </Stack>
  );
}
