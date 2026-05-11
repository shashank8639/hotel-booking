import { z } from 'zod';
import { nightsBetween, todayIso } from './format';

/**
 * Zod schemas for React Hook Form (@hookform/resolvers/zod).
 * Mirrors bookingValidation.js rules for a single source of schema truth.
 */

export const bookingStaySchema = z
  .object({
    checkIn: z.string().min(1, 'Check-in date is required'),
    checkOut: z.string().min(1, 'Check-out date is required'),
    guests: z.coerce.number().int().min(1, 'At least 1 guest is required'),
    specialRequests: z.string().optional().default(''),
  })
  .superRefine((data, ctx) => {
    const today = todayIso();
    if (data.checkIn && data.checkIn < today) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['checkIn'],
        message: 'Check-in cannot be in the past',
      });
    }
    if (data.checkIn && data.checkOut && data.checkOut <= data.checkIn) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['checkOut'],
        message: 'Check-out must be after check-in',
      });
    }
    if (data.checkIn && data.checkOut && nightsBetween(data.checkIn, data.checkOut) < 1) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['checkOut'],
        message: 'Stay must be at least 1 night',
      });
    }
  });

export const guestSchema = z.object({
  firstName: z.string().trim().min(1, 'First name is required').max(100, 'First name is too long'),
  lastName: z.string().trim().min(1, 'Last name is required').max(100, 'Last name is too long'),
  email: z.string().trim().min(1, 'Email is required').email('Enter a valid email address'),
  phone: z
    .string()
    .trim()
    .min(1, 'Phone number is required')
    .regex(/^\+?[0-9\s\-()]{7,20}$/, 'Enter a valid phone number'),
  address: z.string().max(500, 'Address must be under 500 characters').optional().default(''),
});
