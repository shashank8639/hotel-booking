import { useMemo } from 'react';
import { Link as RouterLink, useNavigate, useParams, useSearchParams } from 'react-router-dom';
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
import StarIcon from '@mui/icons-material/Star';
import { useRoomDetails } from '../hooks/useRoomDetails';
import { ImageGallery } from '../components/rooms/ImageGallery';
import { AmenitiesList } from '../components/rooms/AmenitiesList';
import { DetailSkeleton } from '../components/common/LoadingSkeletons';
import { ErrorState } from '../components/common/ErrorState';
import {
  amenitiesForRoomType,
  displayRatingForRoomType,
  PLATFORM,
} from '../assets/hotelContent';
import { formatCurrency } from '../utils/format';
import { useAuth } from '../hooks/useAuth';
import { defaultSearchState } from '../utils/searchParams';

/**
 * Room details — hotel label comes from the room API when present.
 */
export default function RoomDetailsPage() {
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const { room, images, loading, error } = useRoomDetails(id);

  const stay = useMemo(() => {
    const defaults = defaultSearchState();
    return {
      checkIn: searchParams.get('checkIn') || defaults.checkIn,
      checkOut: searchParams.get('checkOut') || defaults.checkOut,
      guests: Number(searchParams.get('guests') || defaults.guests),
    };
  }, [searchParams]);

  const handleBookNow = () => {
    const qs = new URLSearchParams({
      roomId: String(id),
      checkIn: stay.checkIn,
      checkOut: stay.checkOut,
      guests: String(stay.guests),
    }).toString();

    if (!isAuthenticated) {
      navigate('/login', { state: { from: `/book?${qs}` } });
      return;
    }
    navigate(`/book?${qs}`);
  };

  if (loading) {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <DetailSkeleton />
      </Container>
    );
  }

  if (error || !room) {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <ErrorState message={error || 'Room not found'} />
        <Button component={RouterLink} to="/rooms" sx={{ mt: 2 }}>
          Back to search
        </Button>
      </Container>
    );
  }

  const rating = displayRatingForRoomType(room.roomType);
  const hotelName = room.hotelName || PLATFORM.name;
  const hotelPlace = PLATFORM.address;

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Grid container spacing={4}>
        <Grid item xs={12} md={7}>
          <ImageGallery room={room} images={images} />
        </Grid>
        <Grid item xs={12} md={5}>
          <Stack spacing={2}>
            <Typography variant="h4" fontWeight={800}>
              {room.roomType} Room {room.roomNumber}
            </Typography>
            <Typography color="text.secondary">
              {hotelName} · {hotelPlace}
            </Typography>
            <Stack direction="row" spacing={1} alignItems="center">
              <Chip icon={<StarIcon />} label={`${rating.toFixed(1)} rating`} color="warning" variant="outlined" />
              <Chip label={room.status} color={room.status === 'AVAILABLE' ? 'success' : 'default'} />
              <Chip label={`Sleeps ${room.capacity}`} />
            </Stack>
            <Typography variant="h4" fontWeight={800}>
              {formatCurrency(room.effectivePrice ?? room.pricePerNight)}
              <Typography component="span" variant="body1" color="text.secondary">
                {' '}
                / night
              </Typography>
            </Typography>
            <Typography>{room.description || PLATFORM.description}</Typography>
            <Button variant="contained" size="large" onClick={handleBookNow}>
              Book now
            </Button>
            <Typography variant="caption" color="text.secondary">
              Dates: {stay.checkIn} → {stay.checkOut} · Guests: {stay.guests}
              {!isAuthenticated && ' · Login required before booking'}
            </Typography>
          </Stack>
        </Grid>
      </Grid>

      <Divider sx={{ my: 4 }} />

      <Grid container spacing={4}>
        <Grid item xs={12} md={6}>
          <AmenitiesList items={amenitiesForRoomType(room.roomType)} />
          <Box sx={{ mt: 3 }}>
            <Typography fontWeight={700} gutterBottom>
              Policies
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Check-in from 2:00 PM · Check-out by 11:00 AM · Photo ID required ·
              Cancellation rules follow booking status and payment policy.
            </Typography>
          </Box>
          <Box sx={{ mt: 3 }}>
            <Typography fontWeight={700} gutterBottom>
              Location
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {hotelPlace}
            </Typography>
          </Box>
        </Grid>
        <Grid item xs={12} md={6}>
          <Typography fontWeight={700} gutterBottom>
            Reviews
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Reviews section is a UI placeholder for Module 11. Wire a reviews API in a later module.
          </Typography>
          <Box sx={{ mt: 2, p: 2, bgcolor: 'grey.100', borderRadius: 2 }}>
            <Typography variant="body2" sx={{ fontStyle: 'italic' }}>
              “Spacious room and calm corridor — exactly what we needed after a long flight.”
            </Typography>
            <Typography variant="caption" color="text.secondary">
              — Sample guest review
            </Typography>
          </Box>
        </Grid>
      </Grid>
    </Container>
  );
}
