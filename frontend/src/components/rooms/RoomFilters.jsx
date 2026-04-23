import { useEffect, useState } from 'react';
import {
  Box,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import { useDebouncedValue } from '../../hooks/useDebouncedValue';

const SORT_OPTIONS = [
  { value: 'pricePerNight,asc', label: 'Price · low to high' },
  { value: 'pricePerNight,desc', label: 'Price · high to low' },
  { value: 'createdAt,desc', label: 'Newest' },
  { value: 'roomNumber,asc', label: 'Room number' },
];

/**
 * Sidebar filters — maps to GET /rooms/search query params.
 * Price fields are debounced so URL/API updates after typing pauses.
 */
export function RoomFilters({ value, onChange }) {
  const [minPrice, setMinPrice] = useState(value.minPrice ?? '');
  const [maxPrice, setMaxPrice] = useState(value.maxPrice ?? '');
  const debouncedMin = useDebouncedValue(minPrice, 400);
  const debouncedMax = useDebouncedValue(maxPrice, 400);

  useEffect(() => {
    setMinPrice(value.minPrice ?? '');
    setMaxPrice(value.maxPrice ?? '');
  }, [value.minPrice, value.maxPrice]);

  useEffect(() => {
    if (debouncedMin === (value.minPrice ?? '') && debouncedMax === (value.maxPrice ?? '')) {
      return;
    }
    onChange({
      ...value,
      minPrice: debouncedMin,
      maxPrice: debouncedMax,
      page: 0,
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debouncedMin, debouncedMax]);

  const set = (field) => (event) => {
    onChange({ ...value, [field]: event.target.value, page: 0 });
  };

  return (
    <Box sx={{ p: 2, bgcolor: 'background.paper', borderRadius: 2, border: '1px solid', borderColor: 'divider' }}>
      <Typography fontWeight={700} sx={{ mb: 2 }}>
        Filters
      </Typography>
      <Stack spacing={2}>
        <FormControl fullWidth size="small">
          <InputLabel>Room type</InputLabel>
          <Select label="Room type" value={value.roomType || ''} onChange={set('roomType')}>
            <MenuItem value="">Any</MenuItem>
            {['STANDARD', 'DELUXE', 'EXECUTIVE', 'SUITE', 'FAMILY', 'PRESIDENTIAL'].map((t) => (
              <MenuItem key={t} value={t}>
                {t}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <TextField
          label="Min price"
          type="number"
          size="small"
          value={minPrice}
          onChange={(e) => setMinPrice(e.target.value)}
          helperText="Applies after you pause typing"
          fullWidth
        />
        <TextField
          label="Max price"
          type="number"
          size="small"
          value={maxPrice}
          onChange={(e) => setMaxPrice(e.target.value)}
          error={Boolean(minPrice && maxPrice && Number(minPrice) > Number(maxPrice))}
          helperText="Applies after you pause typing"
          fullWidth
        />

        <FormControl fullWidth size="small">
          <InputLabel>Availability</InputLabel>
          <Select label="Availability" value={value.status || 'AVAILABLE'} onChange={set('status')}>
            <MenuItem value="AVAILABLE">Available</MenuItem>
            <MenuItem value="">Any status</MenuItem>
          </Select>
        </FormControl>

        <FormControl fullWidth size="small">
          <InputLabel>Sort</InputLabel>
          <Select label="Sort" value={value.sort} onChange={set('sort')}>
            {SORT_OPTIONS.map((opt) => (
              <MenuItem key={opt.value} value={opt.value}>
                {opt.label}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <Typography variant="caption" color="text.secondary">
          Rating & amenity filters are UI placeholders — backend search supports type, price, capacity, status.
        </Typography>
      </Stack>
    </Box>
  );
}
