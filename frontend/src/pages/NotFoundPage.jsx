import { Button, Container, Typography } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';

export default function NotFoundPage() {
  return (
    <Container maxWidth="sm" sx={{ py: 10, textAlign: 'center' }}>
      <Typography variant="h3" fontWeight={800} gutterBottom>
        404
      </Typography>
      <Typography color="text.secondary" sx={{ mb: 3 }}>
        This page does not exist on the public website.
      </Typography>
      <Button component={RouterLink} to="/" variant="contained">
        Back to home
      </Button>
    </Container>
  );
}
