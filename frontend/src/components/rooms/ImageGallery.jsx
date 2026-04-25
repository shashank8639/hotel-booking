import { useState } from 'react';
import { Box, Grid, Typography } from '@mui/material';
import { primaryImageForRoom, ROOM_TYPE_FALLBACK_IMAGES } from '../../assets/hotelContent';

/**
 * Simple image gallery — primary large image + thumbnails.
 */
export function ImageGallery({ room, images = [] }) {
  const urls =
    images.length > 0
      ? images.map((img) => img.imageUrl).filter(Boolean)
      : [primaryImageForRoom(room), ROOM_TYPE_FALLBACK_IMAGES.DELUXE, ROOM_TYPE_FALLBACK_IMAGES.SUITE];

  const [active, setActive] = useState(0);
  const current = urls[active] || urls[0];

  return (
    <Box>
      <Box
        component="img"
        src={current}
        alt={`Room ${room?.roomNumber || ''}`}
        sx={{
          width: '100%',
          height: { xs: 240, md: 420 },
          objectFit: 'cover',
          borderRadius: 2,
          display: 'block',
        }}
      />
      <Grid container spacing={1} sx={{ mt: 1 }}>
        {urls.slice(0, 5).map((url, index) => (
          <Grid item xs={3} sm={2} key={`${url}-${index}`}>
            <Box
              component="img"
              src={url}
              alt=""
              onClick={() => setActive(index)}
              sx={{
                width: '100%',
                height: 64,
                objectFit: 'cover',
                borderRadius: 1,
                cursor: 'pointer',
                outline: index === active ? '2px solid' : 'none',
                outlineColor: 'primary.main',
              }}
            />
          </Grid>
        ))}
      </Grid>
      {!images.length && (
        <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
          Showing fallback gallery images when the room has no uploaded photos.
        </Typography>
      )}
    </Box>
  );
}
