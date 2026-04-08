import {
  Box,
  Button,
  Card,
  CardActions,
  CardContent,
  CardMedia,
  Chip,
  Stack,
  Typography,
} from '@mui/material';
import StarIcon from '@mui/icons-material/Star';
import { Link as RouterLink } from 'react-router-dom';
import {
  amenitiesForRoomType,
  displayRatingForRoomType,
  PLATFORM,
  primaryImageForRoom,
} from '../../assets/hotelContent';
import { formatCurrency } from '../../utils/format';

/**
 * Room card — shows hotel name from API when available (Module 16).
 */
export function RoomCard({ room, checkIn, checkOut, guests }) {
  const rating = displayRatingForRoomType(room.roomType);
  const amenities = amenitiesForRoomType(room.roomType).slice(0, 3);
  const hotelLabel = room.hotelName || PLATFORM.name;
  const detailQuery = new URLSearchParams({
    ...(checkIn ? { checkIn } : {}),
    ...(checkOut ? { checkOut } : {}),
    ...(guests ? { guests: String(guests) } : {}),
  }).toString();

  return (
    <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }} data-testid="room-card">
      <CardMedia
        component="img"
        height="180"
        image={primaryImageForRoom(room)}
        alt={`Room ${room.roomNumber}`}
      />
      <CardContent sx={{ flexGrow: 1 }}>
        <Stack direction="row" justifyContent="space-between" alignItems="flex-start" spacing={1}>
          <Box>
            <Typography variant="h6" fontWeight={700}>
              {room.roomType} · {room.roomNumber}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {hotelLabel}
              {room.hotelSlug ? '' : ` · ${PLATFORM.location}`}
            </Typography>
          </Box>
          <Chip
            size="small"
            icon={<StarIcon sx={{ fontSize: 16 }} />}
            label={rating.toFixed(1)}
            color="warning"
            variant="outlined"
          />
        </Stack>

        <Typography variant="h5" fontWeight={800} sx={{ mt: 1.5 }}>
          {formatCurrency(room.effectivePrice ?? room.pricePerNight)}
          <Typography component="span" variant="body2" color="text.secondary">
            {' '}
            / night
          </Typography>
        </Typography>

        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
          Sleeps {room.capacity} · Status {room.status}
        </Typography>

        <Stack direction="row" spacing={0.5} flexWrap="wrap" useFlexGap sx={{ mt: 1.5 }}>
          {amenities.map((item) => (
            <Chip key={item} label={item} size="small" />
          ))}
        </Stack>
      </CardContent>
      <CardActions sx={{ px: 2, pb: 2 }}>
        <Button
          component={RouterLink}
          to={`/rooms/${room.id}${detailQuery ? `?${detailQuery}` : ''}`}
          variant="contained"
          fullWidth
        >
          View & Book
        </Button>
      </CardActions>
    </Card>
  );
}
