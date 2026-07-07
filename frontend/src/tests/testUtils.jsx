import { render } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material';

const theme = createTheme();

/**
 * Shared RTL render helper — wraps MUI theme + router for page/component tests.
 */
export function renderWithProviders(ui, { route = '/', ...options } = {}) {
  return render(
    <ThemeProvider theme={theme}>
      <MemoryRouter initialEntries={[route]}>{ui}</MemoryRouter>
    </ThemeProvider>,
    options
  );
}

export { theme };
