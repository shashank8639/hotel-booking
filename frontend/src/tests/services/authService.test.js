import { describe, it, expect, vi, beforeEach } from 'vitest';

vi.mock('../../services/api', () => ({
  default: {
    post: vi.fn(),
    get: vi.fn(),
  },
}));

import api from '../../services/api';
import { authService } from '../../services/authService';

describe('authService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('posts login payload to /auth/login', async () => {
    api.post.mockResolvedValue({ data: { accessToken: 'a' } });
    const result = await authService.login({ email: 'a@b.com', password: 'x' });
    expect(api.post).toHaveBeenCalledWith('/auth/login', { email: 'a@b.com', password: 'x' });
    expect(result.accessToken).toBe('a');
  });

  it('gets current user from /auth/me', async () => {
    api.get.mockResolvedValue({ data: { email: 'a@b.com' } });
    const me = await authService.me();
    expect(api.get).toHaveBeenCalledWith('/auth/me');
    expect(me.email).toBe('a@b.com');
  });
});
