import { useMemo, useState } from 'react';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  Checkbox,
  FormControlLabel,
  IconButton,
  InputAdornment,
  LinearProgress,
  Link,
  Stack,
  TextField,
  Typography,
  CircularProgress,
} from '@mui/material';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import { useAuth } from '../hooks/useAuth';
import { getPasswordStrength, validateRegisterForm } from '../utils/validation';

/**
 * Registration page — POST /api/auth/register (creates CUSTOMER by default).
 */
export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
    rememberMe: false,
  });
  const [showPassword, setShowPassword] = useState(false);
  const [errors, setErrors] = useState({});
  const [apiError, setApiError] = useState('');
  const [success, setSuccess] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const strength = useMemo(() => getPasswordStrength(form.password), [form.password]);

  const update = (field) => (event) => {
    const value = field === 'rememberMe' ? event.target.checked : event.target.value;
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setApiError('');
    setSuccess('');
    const nextErrors = validateRegisterForm(form);
    setErrors(nextErrors);
    if (Object.keys(nextErrors).length) {
      return;
    }

    setSubmitting(true);
    try {
      const result = await register({
        firstName: form.firstName.trim(),
        lastName: form.lastName.trim(),
        email: form.email.trim(),
        password: form.password,
        rememberMe: form.rememberMe,
      });
      setSuccess('Account created successfully. Redirecting…');
      navigate(result.redirectTo || '/customer/dashboard', { replace: true });
    } catch (err) {
      setApiError(err.message || 'Registration failed');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Box component="form" onSubmit={handleSubmit} noValidate>
      <Typography variant="h6" gutterBottom align="center">
        Create account
      </Typography>
      <Typography variant="body2" color="text.secondary" align="center" sx={{ mb: 2 }}>
        Register as a customer to book rooms
      </Typography>

      {apiError && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {apiError}
        </Alert>
      )}
      {success && (
        <Alert severity="success" sx={{ mb: 2 }}>
          {success}
        </Alert>
      )}

      <Stack spacing={2}>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
          <TextField
            label="First name"
            value={form.firstName}
            onChange={update('firstName')}
            error={Boolean(errors.firstName)}
            helperText={errors.firstName}
            fullWidth
            required
          />
          <TextField
            label="Last name"
            value={form.lastName}
            onChange={update('lastName')}
            error={Boolean(errors.lastName)}
            helperText={errors.lastName}
            fullWidth
            required
          />
        </Stack>
        <TextField
          label="Email"
          type="email"
          value={form.email}
          onChange={update('email')}
          error={Boolean(errors.email)}
          helperText={errors.email}
          autoComplete="email"
          fullWidth
          required
        />
        <TextField
          label="Password"
          type={showPassword ? 'text' : 'password'}
          value={form.password}
          onChange={update('password')}
          error={Boolean(errors.password)}
          helperText={errors.password || 'At least 8 characters'}
          autoComplete="new-password"
          fullWidth
          required
          InputProps={{
            endAdornment: (
              <InputAdornment position="end">
                <IconButton
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                  onClick={() => setShowPassword((v) => !v)}
                  edge="end"
                >
                  {showPassword ? <VisibilityOff /> : <Visibility />}
                </IconButton>
              </InputAdornment>
            ),
          }}
        />
        {form.password && (
          <Box>
            <LinearProgress
              variant="determinate"
              value={Math.min((strength.score / 6) * 100, 100)}
              color={strength.color}
              sx={{ height: 8, borderRadius: 1 }}
            />
            <Typography variant="caption" color={`${strength.color}.main`}>
              Password strength: {strength.label}
            </Typography>
          </Box>
        )}
        <TextField
          label="Confirm password"
          type={showPassword ? 'text' : 'password'}
          value={form.confirmPassword}
          onChange={update('confirmPassword')}
          error={Boolean(errors.confirmPassword)}
          helperText={errors.confirmPassword}
          autoComplete="new-password"
          fullWidth
          required
        />
        <FormControlLabel
          control={<Checkbox checked={form.rememberMe} onChange={update('rememberMe')} />}
          label="Remember me"
        />
        <Button type="submit" variant="contained" size="large" disabled={submitting} fullWidth>
          {submitting ? <CircularProgress size={24} color="inherit" /> : 'Register'}
        </Button>
        <Typography variant="body2" align="center">
          Already have an account?{' '}
          <Link component={RouterLink} to="/login">
            Login
          </Link>
        </Typography>
      </Stack>
    </Box>
  );
}
