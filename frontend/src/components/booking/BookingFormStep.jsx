import { useEffect, useMemo, useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  Alert,
  Box,
  Button,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import { useBookingWizard } from '../../hooks/useBookingWizard';
import { bookingService } from '../../services/bookingService';
import { roomService } from '../../services/roomService';
import { bookingStaySchema } from '../../utils/bookingSchemas';
import { nightsBetween, formatCurrency } from '../../utils/format';
import RoomMultiSelect from './RoomMultiSelect';

/**
 * Step 1 — dates, guest count, up to 2 rooms, special requests.
 * Zod + RHF; blocks Continue when availability is false.
 */
export default function BookingFormStep({ onNext }) {
  const {
    room,
    roomId,
    roomIds,
    rooms,
    checkIn,
    checkOut,
    guests,
    specialRequests,
    availabilityNote,
    availabilityOk,
    totalCapacity,
    setBookingFields,
    setRooms,
    setAvailability,
  } = useBookingWizard();

  const [catalog, setCatalog] = useState([]);
  const [roomError, setRoomError] = useState('');

  const {
    control,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm({
    defaultValues: {
      checkIn,
      checkOut,
      guests,
      specialRequests,
    },
    resolver: zodResolver(bookingStaySchema),
    mode: 'onBlur',
  });

  const watchedIn = watch('checkIn');
  const watchedOut = watch('checkOut');
  const watchedGuests = watch('guests');
  const liveNights = nightsBetween(watchedIn, watchedOut);

  useEffect(() => {
    let cancelled = false;
    roomService
      .search({ size: 30, page: 0 })
      .then((page) => {
        if (!cancelled) {
          const content = page.content || page || [];
          setCatalog(Array.isArray(content) ? content : []);
        }
      })
      .catch(() => {
        if (!cancelled && room) setCatalog([room]);
      });
    return () => {
      cancelled = true;
    };
  }, [room]);

  const options = useMemo(() => {
    const map = new Map((catalog || []).map((r) => [r.id, r]));
    (rooms || []).forEach((r) => map.set(r.id, r));
    if (room) map.set(room.id, room);
    return [...map.values()];
  }, [catalog, rooms, room]);

  useEffect(() => {
    let cancelled = false;
    async function probe() {
      const ids = roomIds?.length ? roomIds : roomId ? [roomId] : [];
      if (!ids.length || !watchedIn || !watchedOut || watchedOut <= watchedIn) {
        setAvailability('', null);
        return;
      }
      try {
        const result = await bookingService.checkAvailability({
          checkInDate: watchedIn,
          checkOutDate: watchedOut,
          roomIds: ids.map(Number),
        });
        const items = result.rooms || [];
        const allOk = items.length > 0 && items.every((item) => item.available);
        const blocked = items.find((item) => !item.available);
        if (!cancelled) {
          setAvailability(
            allOk
              ? `${ids.length} room(s) available for these dates.`
              : blocked?.reason || 'One or more rooms are not available for these dates.',
            allOk
          );
        }
      } catch (err) {
        if (!cancelled) setAvailability(err.message || 'Could not verify availability', false);
      }
    }
    probe();
    return () => {
      cancelled = true;
    };
  }, [roomIds, roomId, watchedIn, watchedOut, setAvailability]);

  const handleRoomIdsChange = (ids) => {
    setRoomError('');
    const selected = ids
      .map((id) => options.find((r) => r.id === id))
      .filter(Boolean);
    setRooms(selected);
  };

  const onSubmit = (values) => {
    const ids = roomIds?.length ? roomIds : roomId ? [roomId] : [];
    if (!ids.length) {
      setRoomError('Please select at least one room');
      return;
    }
    if (totalCapacity && Number(values.guests) > totalCapacity) {
      setRoomError(`Selected rooms sleep up to ${totalCapacity} guests`);
      return;
    }
    if (availabilityOk === false) {
      return;
    }
    setBookingFields({
      checkIn: values.checkIn,
      checkOut: values.checkOut,
      guests: Number(values.guests),
      specialRequests: values.specialRequests || '',
      roomIds: ids,
    });
    onNext();
  };

  const continueBlocked = availabilityOk === false;

  return (
    <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
      <Typography variant="h5" fontWeight={700} gutterBottom>
        Select stay details
      </Typography>
      <Typography color="text.secondary" sx={{ mb: 2 }}>
        {room
          ? `Starting from ${room.roomType} · Room ${room.roomNumber} · ${formatCurrency(room.effectivePrice ?? room.pricePerNight)}/night`
          : 'Choose up to two rooms for this stay.'}
      </Typography>

      <Stack spacing={2}>
        <RoomMultiSelect
          options={options}
          selectedIds={roomIds?.length ? roomIds : roomId ? [roomId] : []}
          primaryRoomId={roomId}
          onChange={handleRoomIdsChange}
          error={roomError}
        />

        <Controller
          name="checkIn"
          control={control}
          render={({ field }) => (
            <TextField
              {...field}
              label="Check-in date"
              type="date"
              InputLabelProps={{ shrink: true }}
              error={Boolean(errors.checkIn)}
              helperText={errors.checkIn?.message}
              required
              fullWidth
            />
          )}
        />
        <Controller
          name="checkOut"
          control={control}
          render={({ field }) => (
            <TextField
              {...field}
              label="Check-out date"
              type="date"
              InputLabelProps={{ shrink: true }}
              error={Boolean(errors.checkOut)}
              helperText={errors.checkOut?.message}
              required
              fullWidth
            />
          )}
        />

        <Typography
          variant="body2"
          color={liveNights > 0 ? 'primary.main' : 'text.secondary'}
          data-testid="nights-live-label"
          fontWeight={600}
        >
          {liveNights > 0
            ? `${liveNights} night${liveNights === 1 ? '' : 's'} selected`
            : 'Select check-in and check-out to see nights'}
        </Typography>

        <Controller
          name="guests"
          control={control}
          render={({ field }) => (
            <TextField
              {...field}
              label="Guest count"
              type="number"
              inputProps={{ min: 1, max: totalCapacity || 20 }}
              error={Boolean(errors.guests)}
              helperText={
                errors.guests?.message ||
                (totalCapacity
                  ? `Combined capacity: ${totalCapacity} · currently ${watchedGuests || 0} guests`
                  : undefined)
              }
              fullWidth
            />
          )}
        />
        <Controller
          name="specialRequests"
          control={control}
          render={({ field }) => (
            <TextField
              {...field}
              label="Special requests"
              multiline
              minRows={3}
              fullWidth
              placeholder="Late check-in, extra pillows, accessibility needs…"
            />
          )}
        />

        {availabilityNote && (
          <Alert severity={availabilityOk === false ? 'warning' : 'info'}>
            {availabilityNote}
          </Alert>
        )}
        {continueBlocked && (
          <Alert severity="error" data-testid="availability-blocked">
            Continue is blocked until all selected rooms are available for these dates.
          </Alert>
        )}

        <Button
          type="submit"
          variant="contained"
          size="large"
          disabled={continueBlocked}
          data-testid="continue-guest"
        >
          Continue to guest details
        </Button>
      </Stack>
    </Box>
  );
}
