import { useEffect } from 'react';
import { Controller, useForm } from 'react-hook-form';
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
  TextField,
} from '@mui/material';

const empty = { firstName: '', lastName: '', email: '', phone: '' };

export default function GuestFormDialog({ open, guest, onClose, onSubmit, loading }) {
  const { control, handleSubmit, reset } = useForm({ defaultValues: empty });

  useEffect(() => {
    if (!open) return;
    reset(
      guest
        ? {
            firstName: guest.firstName || '',
            lastName: guest.lastName || '',
            email: guest.email || '',
            phone: guest.phone || '',
          }
        : empty
    );
  }, [open, guest, reset]);

  return (
    <Dialog open={open} onClose={loading ? undefined : onClose} fullWidth maxWidth="sm">
      <DialogTitle>{guest ? 'Edit guest' : 'Create guest'}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }} component="form" id="guest-form" onSubmit={handleSubmit(onSubmit)}>
          {['firstName', 'lastName', 'email', 'phone'].map((name) => (
            <Controller
              key={name}
              name={name}
              control={control}
              rules={{ required: 'Required' }}
              render={({ field, fieldState }) => (
                <TextField
                  {...field}
                  label={name === 'firstName' ? 'First name' : name === 'lastName' ? 'Last name' : name === 'email' ? 'Email' : 'Phone'}
                  error={Boolean(fieldState.error)}
                  helperText={fieldState.error?.message}
                  fullWidth
                  required
                />
              )}
            />
          ))}
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={loading}>Cancel</Button>
        <Button type="submit" form="guest-form" variant="contained" disabled={loading}>
          Save
        </Button>
      </DialogActions>
    </Dialog>
  );
}
