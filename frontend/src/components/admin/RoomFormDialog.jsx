import { useEffect } from 'react';
import { Controller, useForm } from 'react-hook-form';
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  Stack,
  TextField,
} from '@mui/material';

const ROOM_TYPES = ['STANDARD', 'DELUXE', 'EXECUTIVE', 'SUITE', 'FAMILY', 'PRESIDENTIAL'];
const ROOM_STATUSES = ['AVAILABLE', 'RESERVED', 'OCCUPIED', 'MAINTENANCE', 'OUT_OF_SERVICE'];

const empty = {
  roomNumber: '',
  roomType: 'STANDARD',
  floorNumber: 1,
  capacity: 2,
  pricePerNight: 3000,
  discountedPrice: '',
  status: 'AVAILABLE',
  description: '',
};

export default function RoomFormDialog({ open, room, onClose, onSubmit, loading }) {
  const { control, handleSubmit, reset } = useForm({ defaultValues: empty });

  useEffect(() => {
    if (!open) return;
    reset(
      room
        ? {
            roomNumber: room.roomNumber || '',
            roomType: room.roomType || 'STANDARD',
            floorNumber: room.floorNumber ?? 1,
            capacity: room.capacity ?? 2,
            pricePerNight: room.pricePerNight ?? 0,
            discountedPrice: room.discountedPrice ?? '',
            status: room.status || 'AVAILABLE',
            description: room.description || '',
          }
        : empty
    );
  }, [open, room, reset]);

  return (
    <Dialog open={open} onClose={loading ? undefined : onClose} fullWidth maxWidth="sm">
      <DialogTitle>{room ? 'Edit room' : 'Create room'}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }} component="form" id="room-form" onSubmit={handleSubmit(onSubmit)}>
          <Controller
            name="roomNumber"
            control={control}
            rules={{ required: 'Required' }}
            render={({ field, fieldState }) => (
              <TextField {...field} label="Room number" error={Boolean(fieldState.error)} helperText={fieldState.error?.message} fullWidth required />
            )}
          />
          <Controller
            name="roomType"
            control={control}
            render={({ field }) => (
              <TextField {...field} select label="Room type" fullWidth>
                {ROOM_TYPES.map((t) => (
                  <MenuItem key={t} value={t}>{t}</MenuItem>
                ))}
              </TextField>
            )}
          />
          <Controller
            name="status"
            control={control}
            render={({ field }) => (
              <TextField {...field} select label="Status" fullWidth>
                {ROOM_STATUSES.map((t) => (
                  <MenuItem key={t} value={t}>{t}</MenuItem>
                ))}
              </TextField>
            )}
          />
          <Controller
            name="floorNumber"
            control={control}
            render={({ field }) => <TextField {...field} type="number" label="Floor" fullWidth />}
          />
          <Controller
            name="capacity"
            control={control}
            rules={{ min: 1 }}
            render={({ field }) => <TextField {...field} type="number" label="Capacity" fullWidth required />}
          />
          <Controller
            name="pricePerNight"
            control={control}
            rules={{ required: 'Required' }}
            render={({ field }) => <TextField {...field} type="number" label="Price / night" fullWidth required />}
          />
          <Controller
            name="discountedPrice"
            control={control}
            render={({ field }) => <TextField {...field} type="number" label="Discounted price" fullWidth />}
          />
          <Controller
            name="description"
            control={control}
            render={({ field }) => <TextField {...field} label="Description" multiline minRows={2} fullWidth />}
          />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={loading}>Cancel</Button>
        <Button type="submit" form="room-form" variant="contained" disabled={loading}>
          {room ? 'Save' : 'Create'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
