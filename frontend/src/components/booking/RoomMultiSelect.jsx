import {
  Checkbox,
  FormControl,
  FormHelperText,
  InputLabel,
  ListItemText,
  MenuItem,
  OutlinedInput,
  Select,
  Typography,
} from '@mui/material';
import { formatCurrency } from '../../utils/format';

const MAX_ROOMS = 2;

/**
 * Multi-select up to 2 rooms for one booking (maps to BookingRequest.roomIds).
 */
export default function RoomMultiSelect({
  options = [],
  selectedIds = [],
  primaryRoomId,
  onChange,
  error,
}) {
  const handleChange = (event) => {
    const value = event.target.value;
    let next = typeof value === 'string' ? value.split(',').map(Number) : value.map(Number);

    // Always keep the deep-linked primary room if present
    if (primaryRoomId && !next.includes(Number(primaryRoomId))) {
      next = [Number(primaryRoomId), ...next];
    }
    next = [...new Set(next)].slice(0, MAX_ROOMS);
    onChange(next);
  };

  return (
    <FormControl fullWidth error={Boolean(error)}>
      <InputLabel id="room-multi-label">Rooms (max {MAX_ROOMS})</InputLabel>
      <Select
        labelId="room-multi-label"
        multiple
        value={selectedIds}
        onChange={handleChange}
        input={<OutlinedInput label={`Rooms (max ${MAX_ROOMS})`} />}
        renderValue={(ids) =>
          ids
            .map((id) => {
              const room = options.find((r) => r.id === id);
              return room ? `${room.roomType} ${room.roomNumber}` : `#${id}`;
            })
            .join(', ')
        }
        data-testid="room-multi-select"
      >
        {options.map((room) => {
          const checked = selectedIds.includes(room.id);
          const disableExtra =
            !checked && selectedIds.length >= MAX_ROOMS && room.id !== primaryRoomId;
          return (
            <MenuItem key={room.id} value={room.id} disabled={disableExtra}>
              <Checkbox checked={checked} />
              <ListItemText
                primary={`${room.roomType} · ${room.roomNumber}`}
                secondary={`${formatCurrency(room.effectivePrice ?? room.pricePerNight)}/night · sleeps ${room.capacity}`}
              />
            </MenuItem>
          );
        })}
      </Select>
      <FormHelperText>
        {error ||
          `Select up to ${MAX_ROOMS} rooms. Primary room from “Book Now” stays selected.`}
      </FormHelperText>
      <Typography variant="caption" color="text.secondary">
        {selectedIds.length} of {MAX_ROOMS} selected
      </Typography>
    </FormControl>
  );
}
