import { Box, Card, CardContent, CardMedia, Chip, Grid, Stack, Typography } from '@mui/material';
import { DESTINATIONS, SERVICES, TESTIMONIALS } from '../../assets/hotelContent';

export function SectionHeader({ title, subtitle }) {
  return (
    <Box sx={{ mb: 3, maxWidth: 640 }}>
      <Typography variant="h4" fontWeight={750} gutterBottom>
        {title}
      </Typography>
      {subtitle && (
        <Typography color="text.secondary">{subtitle}</Typography>
      )}
    </Box>
  );
}

export function DestinationsSection() {
  return (
    <Box>
      <SectionHeader
        title="Popular destinations"
        subtitle="Marketing highlights for the brand. This property currently operates in Mumbai; other cities are placeholders."
      />
      <Grid container spacing={2}>
        {DESTINATIONS.map((dest) => (
          <Grid item xs={12} sm={6} md={3} key={dest.id}>
            <Card sx={{ height: '100%' }}>
              <CardMedia component="img" height="140" image={dest.image} alt={dest.name} />
              <CardContent>
                <Typography fontWeight={700}>{dest.name}</Typography>
                <Typography variant="body2" color="text.secondary">
                  {dest.blurb}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
}

export function ServicesSection() {
  return (
    <Box>
      <SectionHeader title="Why stay with us" subtitle="Service pillars guests notice on every trip." />
      <Grid container spacing={2}>
        {SERVICES.map((service) => (
          <Grid item xs={12} sm={6} md={3} key={service.title}>
            <Box sx={{ p: 2.5, bgcolor: 'background.paper', borderRadius: 2, height: '100%', border: '1px solid', borderColor: 'divider' }}>
              <Typography fontWeight={700} gutterBottom>
                {service.title}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {service.body}
              </Typography>
            </Box>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
}

export function TestimonialsSection() {
  return (
    <Box>
      <SectionHeader title="Guest stories" subtitle="UI testimonials (placeholder quotes for Module 11)." />
      <Grid container spacing={2}>
        {TESTIMONIALS.map((item) => (
          <Grid item xs={12} md={4} key={item.name}>
            <Box sx={{ p: 3, bgcolor: 'grey.100', borderRadius: 2, height: '100%' }}>
              <Typography variant="body1" sx={{ fontStyle: 'italic', mb: 2 }}>
                “{item.quote}”
              </Typography>
              <Typography fontWeight={700}>{item.name}</Typography>
              <Typography variant="caption" color="text.secondary">
                {item.role}
              </Typography>
            </Box>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
}

export function NewsletterSection() {
  return (
    <Box
      sx={{
        p: { xs: 3, md: 4 },
        borderRadius: 3,
        background: 'linear-gradient(120deg, #1565c0 0%, #00897b 100%)',
        color: 'common.white',
      }}
    >
      <Stack spacing={1} maxWidth={560}>
        <Typography variant="h5" fontWeight={700}>
          Get seasonal offers
        </Typography>
        <Typography variant="body2" sx={{ opacity: 0.9 }}>
          Newsletter capture is a UI placeholder — connect Module 8 email later.
        </Typography>
        <Chip label="Coming soon" sx={{ alignSelf: 'flex-start', bgcolor: 'rgba(255,255,255,0.2)', color: 'white' }} />
      </Stack>
    </Box>
  );
}

export function CtaSection() {
  return (
    <Box sx={{ textAlign: 'center', py: 2 }}>
      <Typography variant="h4" fontWeight={750} gutterBottom>
        Ready for your next stay?
      </Typography>
      <Typography color="text.secondary" sx={{ mb: 2 }}>
        Browse available rooms and lock dates in a few steps.
      </Typography>
    </Box>
  );
}
