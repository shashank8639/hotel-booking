import { Container, Typography, Box } from '@mui/material';

/**
 * Root application component.
 * Pages and routing will be added in later modules.
 */
function App() {
  return (
    <Container maxWidth="lg">
      <Box sx={{ py: 8, textAlign: 'center' }}>
        <Typography variant="h3" component="h1" gutterBottom>
          Hotel Booking System
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Module 1: Project structure initialized. Business features coming soon.
        </Typography>
      </Box>
    </Container>
  );
}

export default App;
