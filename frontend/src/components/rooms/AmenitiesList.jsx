import { Box, Chip, Stack, Typography } from '@mui/material';

export function AmenitiesList({ items = [] }) {
  if (!items.length) return null;
  return (
    <Box>
      <Typography fontWeight={700} gutterBottom>
        Amenities
      </Typography>
      <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
        {items.map((item) => (
          <Chip key={item} label={item} />
        ))}
      </Stack>
    </Box>
  );
}
