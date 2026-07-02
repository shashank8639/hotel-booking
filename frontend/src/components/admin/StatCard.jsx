import { Card, CardContent, Stack, Typography } from '@mui/material';

/** KPI tile for dashboard home. */
export default function StatCard({ title, value, subtitle, icon: Icon, color = 'primary.main' }) {
  return (
    <Card variant="outlined" data-testid="stat-card" sx={{ height: '100%' }}>
      <CardContent>
        <Stack direction="row" justifyContent="space-between" alignItems="flex-start" spacing={1}>
          <BoxText>
            <Typography variant="body2" color="text.secondary">
              {title}
            </Typography>
            <Typography variant="h5" fontWeight={750} sx={{ mt: 0.5 }}>
              {value}
            </Typography>
            {subtitle && (
              <Typography variant="caption" color="text.secondary">
                {subtitle}
              </Typography>
            )}
          </BoxText>
          {Icon && (
            <Icon sx={{ color, fontSize: 32, opacity: 0.85 }} aria-hidden />
          )}
        </Stack>
      </CardContent>
    </Card>
  );
}

function BoxText({ children }) {
  return <Stack spacing={0.25} sx={{ minWidth: 0 }}>{children}</Stack>;
}
