import { useEffect, useState } from 'react';
import { Link as RouterLink, useParams } from 'react-router-dom';
import {
  Box,
  Button,
  Chip,
  Container,
  Divider,
  Grid,
  Stack,
  Typography,
} from '@mui/material';
import { hotelService } from '../services/hotelService';
import { RoomCard } from '../components/rooms/RoomCard';
import { ErrorState } from '../components/common/ErrorState';
import { usePageMeta } from '../utils/practiceStores';

export default function HotelDetailsPage() {
  const { slug } = useParams();
  const [hotel, setHotel] = useState(null);
  const [rooms, setRooms] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  usePageMeta({
    title: hotel ? `${hotel.summary.name} · ${hotel.summary.cityName}` : 'Hotel details',
    description: hotel?.summary?.description || 'Hotel details',
  });

  useEffect(() => {
    let cancelled = false;
    async function load() {
      setLoading(true);
      setError('');
      try {
        const [detail, hotelRooms] = await Promise.all([
          hotelService.getBySlug(slug),
          hotelService.rooms(slug),
        ]);
        if (!cancelled) {
          setHotel(detail);
          setRooms(hotelRooms || []);
        }
      } catch (err) {
        if (!cancelled) setError(err.message || 'Hotel not found');
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    load();
    return () => {
      cancelled = true;
    };
  }, [slug]);

  if (loading) {
    return (
      <Container sx={{ py: 6 }}>
        <Typography>Loading hotel…</Typography>
      </Container>
    );
  }

  if (error || !hotel) {
    return (
      <Container sx={{ py: 6 }}>
        <ErrorState message={error || 'Hotel not found'} />
        <Button component={RouterLink} to="/hotels" sx={{ mt: 2 }}>
          Back to hotels
        </Button>
      </Container>
    );
  }

  const s = hotel.summary;
  const images = hotel.images?.length
    ? hotel.images
    : [{ imageUrl: s.primaryImageUrl, caption: s.name }];

  return (
    <Box>
      <Box
        sx={{
          height: { xs: 240, md: 360 },
          backgroundImage: `linear-gradient(180deg, rgba(0,0,0,0.15), rgba(0,0,0,0.55)), url(${images[0]?.imageUrl})`,
          backgroundSize: 'cover',
          backgroundPosition: 'center',
          color: 'common.white',
          display: 'flex',
          alignItems: 'flex-end',
        }}
      >
        <Container maxWidth="lg" sx={{ pb: 3 }}>
          <Typography variant="h3" fontWeight={800}>
            {s.name}
          </Typography>
          <Typography>
            {s.cityName}, {s.stateName} · {s.starRating}★ · Guest score {Number(s.avgRating).toFixed(1)}
          </Typography>
        </Container>
      </Box>

      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Stack spacing={4}>
          <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
            <Chip label={s.category} />
            {s.breakfastIncluded && <Chip label="Breakfast included" color="success" variant="outlined" />}
            {s.freeCancellation && <Chip label="Free cancellation" color="success" variant="outlined" />}
            {s.petFriendly && <Chip label="Pet friendly" variant="outlined" />}
            {hotel.amenities?.map((a) => (
              <Chip key={a.code} label={a.name} variant="outlined" />
            ))}
          </Stack>

          <Typography color="text.secondary">{s.description}</Typography>
          <Typography variant="body2">
            {hotel.addressLine1}
            {hotel.addressLine2 ? `, ${hotel.addressLine2}` : ''} · Check-in {hotel.checkInTime} · Check-out{' '}
            {hotel.checkOutTime}
          </Typography>

          {hotel.policies?.length > 0 && (
            <Box>
              <Typography variant="h6" fontWeight={700} gutterBottom>
                Policies
              </Typography>
              {hotel.policies.map((p) => (
                <Box key={p.id} sx={{ mb: 1.5 }}>
                  <Typography fontWeight={600}>{p.title}</Typography>
                  <Typography variant="body2" color="text.secondary">
                    {p.body}
                  </Typography>
                </Box>
              ))}
            </Box>
          )}

          <Divider />

          <Box>
            <Typography variant="h5" fontWeight={800} gutterBottom>
              Available rooms
            </Typography>
            {rooms.length === 0 ? (
              <Typography color="text.secondary">
                No rooms listed yet for this hotel.
              </Typography>
            ) : (
              <Grid container spacing={2}>
                {rooms.map((room) => (
                  <Grid item xs={12} sm={6} md={4} key={room.id}>
                    <RoomCard room={room} />
                  </Grid>
                ))}
              </Grid>
            )}
          </Box>

          <Box>
            <Typography variant="h5" fontWeight={800} gutterBottom>
              Guest reviews
            </Typography>
            {hotel.reviews?.length ? (
              hotel.reviews.map((r) => (
                <Box key={r.id} sx={{ mb: 2 }}>
                  <Typography fontWeight={700}>
                    {r.guestName} · {r.rating}/5 — {r.title}
                  </Typography>
                  <Typography color="text.secondary">{r.body}</Typography>
                </Box>
              ))
            ) : (
              <Typography color="text.secondary">No reviews yet.</Typography>
            )}
          </Box>

          <Button component={RouterLink} to="/hotels" variant="outlined">
            Back to search
          </Button>
        </Stack>
      </Container>
    </Box>
  );
}
