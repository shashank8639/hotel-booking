import { Box, Typography } from '@mui/material';
import InboxOutlinedIcon from '@mui/icons-material/InboxOutlined';

/**
 * Empty table illustration — clearer than a blank tbody.
 */
export default function AdminEmptyState({
  title = 'Nothing here yet',
  description = 'Try adjusting search or filters.',
  icon: Icon = InboxOutlinedIcon,
}) {
  return (
    <Box
      sx={{ py: 6, textAlign: 'center', color: 'text.secondary' }}
      data-testid="admin-empty-state"
    >
      <Icon sx={{ fontSize: 56, mb: 1, opacity: 0.45 }} aria-hidden />
      <Typography fontWeight={700} color="text.primary">
        {title}
      </Typography>
      <Typography variant="body2" sx={{ mt: 0.5 }}>
        {description}
      </Typography>
    </Box>
  );
}
