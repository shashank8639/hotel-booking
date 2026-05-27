import { describe, it, expect } from 'vitest';
import { screen } from '@testing-library/react';
import HotelIcon from '@mui/icons-material/Hotel';
import StatCard from '../../components/admin/StatCard';
import { renderWithProviders } from '../testUtils';

describe('StatCard', () => {
  it('renders KPI title and value', () => {
    renderWithProviders(
      <StatCard title="Total Rooms" value={42} subtitle="Available now" icon={HotelIcon} />
    );

    expect(screen.getByTestId('stat-card')).toBeInTheDocument();
    expect(screen.getByText('Total Rooms')).toBeInTheDocument();
    expect(screen.getByText('42')).toBeInTheDocument();
    expect(screen.getByText('Available now')).toBeInTheDocument();
  });
});
