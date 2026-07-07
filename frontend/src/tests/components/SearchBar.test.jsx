import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, fireEvent } from '@testing-library/react';
import { SearchBar } from '../../components/home/SearchBar';
import { renderWithProviders } from '../testUtils';

const navigate = vi.fn();

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => navigate,
  };
});

describe('SearchBar', () => {
  beforeEach(() => {
    navigate.mockClear();
  });

  it('navigates to /rooms with query params on submit', () => {
    renderWithProviders(<SearchBar />);

    fireEvent.click(screen.getByRole('button', { name: /search/i }));

    expect(navigate).toHaveBeenCalled();
    expect(navigate.mock.calls[0][0]).toMatch(/^\/rooms\?/);
  });
});
