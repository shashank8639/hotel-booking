import { describe, it, expect } from 'vitest';
import { screen } from '@testing-library/react';
import ChartCard from '../../charts/ChartCard';
import { renderWithProviders } from '../testUtils';

describe('ChartCard', () => {
  it('renders chart title and children', () => {
    renderWithProviders(
      <ChartCard title="Monthly Revenue">
        <div data-testid="chart-body">chart</div>
      </ChartCard>
    );

    expect(screen.getByText('Monthly Revenue')).toBeInTheDocument();
    expect(screen.getByTestId('chart-body')).toBeInTheDocument();
  });
});
