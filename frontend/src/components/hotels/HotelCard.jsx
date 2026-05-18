import { Link as RouterLink } from 'react-router-dom';
import {
  Box,
  Card,
  CardActionArea,
  CardContent,
  CardMedia,
  Chip,
  Stack,
  Typography,
} from '@mui/material';
import StarIcon from '@mui/icons-material/Star';

export default function HotelCard({ hotel }) {
  const img =
    hotel.primaryImageUrl ||
    'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=80';

  return (
    <Card elevation={1} sx={{ height: '100%' }}>
      <CardActionArea component={RouterLink} to={`/hotels/${hotel.slug}`} sx={{ height: '100%' }}>
        <CardMedia component="img" height="180" image={img} alt={hotel.name} />
        <CardContent>
          <Stack spacing={0.75}>
            <Typography variant="h6" fontWeight={700} lineHeight={1.25}>
              {hotel.name}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {hotel.cityName}, {hotel.stateName}
            </Typography>
            <Stack direction="row" spacing={1} alignItems="center" flexWrap="wrap" useFlexGap>
              <Chip size="small" label={`${hotel.starRating}★`} />
              <Stack direction="row" spacing={0.5} alignItems="center">
                <StarIcon fontSize="small" color="warning" />
                <Typography variant="body2">{Number(hotel.avgRating).toFixed(1)}</Typography>
              </Stack>
              {hotel.freeCancellation && <Chip size="small" label="Free cancel" color="success" variant="outlined" />}
              {hotel.breakfastIncluded && <Chip size="small" label="Breakfast" variant="outlined" />}
            </Stack>
            <Typography variant="subtitle1" fontWeight={700}>
              {hotel.minPrice != null ? `From ₹${hotel.minPrice}` : 'View rates'}
            </Typography>
          </Stack>
        </CardContent>
      </CardActionArea>
    </Card>
  );
}
