import { Card, CardContent, Skeleton, Typography } from '@mui/material';

/** Consistent chart shell with title + loading skeleton. */
export default function ChartCard({ title, subtitle, loading, height = 280, children }) {
  return (
    <Card variant="outlined" data-testid="chart-card" sx={{ height: '100%' }}>
      <CardContent>
        <Typography fontWeight={700} gutterBottom>
          {title}
        </Typography>
        {subtitle && (
          <Typography variant="caption" color="text.secondary" display="block" sx={{ mb: 1 }}>
            {subtitle}
          </Typography>
        )}
        {loading ? <Skeleton variant="rounded" height={height} /> : children}
      </CardContent>
    </Card>
  );
}
