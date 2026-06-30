import { Box } from '@mui/material';

/** Simple responsive CSS grid — avoids Grid2 import variance across MUI builds. */
export function AdminGrid({ children, minWidth = 240 }) {
  return (
    <Box
      sx={{
        display: 'grid',
        gap: 2,
        gridTemplateColumns: {
          xs: '1fr',
          sm: `repeat(auto-fill, minmax(${minWidth}px, 1fr))`,
        },
      }}
    >
      {children}
    </Box>
  );
}

export function AdminGridItem({ children, span = 1 }) {
  return (
    <Box sx={{ gridColumn: span === 'full' ? '1 / -1' : undefined }}>{children}</Box>
  );
}
