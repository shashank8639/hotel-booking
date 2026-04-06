import { useEffect } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Alert, Box, Button, Stack, TextField, Typography } from '@mui/material';
import { useAuth } from '../../hooks/useAuth';
import { useBookingWizard } from '../../hooks/useBookingWizard';
import { guestSchema } from '../../utils/bookingSchemas';

/**
 * Step 2 — guest information with Zod + React Hook Form + auto-fill from user.
 */
export default function GuestFormStep({ onNext, onBack }) {
  const { user } = useAuth();
  const { guest, setGuestFields } = useBookingWizard();

  const {
    control,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({
    defaultValues: guest,
    resolver: zodResolver(guestSchema),
    mode: 'onBlur',
  });

  useEffect(() => {
    if (!user) return;
    const filled = {
      firstName: guest.firstName || user.firstName || '',
      lastName: guest.lastName || user.lastName || '',
      email: user.email || guest.email || '',
      phone: guest.phone || '',
      address: guest.address || '',
    };
    reset(filled);
    setGuestFields(filled);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.email]);

  const onSubmit = (values) => {
    const next = {
      ...values,
      email: user?.email || values.email,
    };
    setGuestFields(next);
    onNext();
  };

  return (
    <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
      <Typography variant="h5" fontWeight={700} gutterBottom>
        Guest information
      </Typography>
      <Typography color="text.secondary" sx={{ mb: 2 }}>
        We use this profile for the booking party (Guest entity — separate from your login User).
      </Typography>

      {user?.email && (
        <Alert severity="info" sx={{ mb: 2 }}>
          Prefilling from your account: {user.email}
        </Alert>
      )}

      <Stack spacing={2}>
        <Controller
          name="firstName"
          control={control}
          render={({ field }) => (
            <TextField
              {...field}
              label="First name"
              error={Boolean(errors.firstName)}
              helperText={errors.firstName?.message}
              required
              fullWidth
              autoComplete="given-name"
            />
          )}
        />
        <Controller
          name="lastName"
          control={control}
          render={({ field }) => (
            <TextField
              {...field}
              label="Last name"
              error={Boolean(errors.lastName)}
              helperText={errors.lastName?.message}
              required
              fullWidth
              autoComplete="family-name"
            />
          )}
        />
        <Controller
          name="email"
          control={control}
          render={({ field }) => (
            <TextField
              {...field}
              label="Email"
              type="email"
              error={Boolean(errors.email)}
              helperText={
                errors.email?.message ||
                (user?.email
                  ? 'Locked to your login email (required for booking ownership)'
                  : undefined)
              }
              required
              fullWidth
              autoComplete="email"
              InputProps={{ readOnly: Boolean(user?.email) }}
            />
          )}
        />
        <Controller
          name="phone"
          control={control}
          render={({ field }) => (
            <TextField
              {...field}
              label="Phone number"
              error={Boolean(errors.phone)}
              helperText={errors.phone?.message || 'Include country code, e.g. +91 9876543210'}
              required
              fullWidth
              autoComplete="tel"
            />
          )}
        />
        <Controller
          name="address"
          control={control}
          render={({ field }) => (
            <TextField
              {...field}
              label="Address"
              multiline
              minRows={2}
              error={Boolean(errors.address)}
              helperText={
                errors.address?.message ||
                'Optional — stored with special requests (Guest API has no address field)'
              }
              fullWidth
              autoComplete="street-address"
            />
          )}
        />

        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
          <Button type="button" variant="outlined" onClick={onBack}>
            Back
          </Button>
          <Button type="submit" variant="contained" size="large">
            Continue to summary
          </Button>
        </Stack>
      </Stack>
    </Box>
  );
}
