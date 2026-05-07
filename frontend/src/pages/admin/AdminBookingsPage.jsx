import { useCallback, useMemo, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  Stack,
  Step,
  StepLabel,
  Stepper,
  TextField,
  Typography,
} from '@mui/material';
import AdminPageHeader from '../../components/admin/AdminPageHeader';
import ConfirmDialog from '../../components/admin/ConfirmDialog';
import AdminDataTable from '../../components/admin/AdminDataTable';
import { usePagedResource } from '../../hooks/usePagedResource';
import { useAdminUi } from '../../context/AdminUiContext';
import { bookingService } from '../../services/bookingService';
import { formatCurrency, formatDateLabel } from '../../utils/format';

const STATUSES = ['PENDING', 'CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED'];
const TIMELINE = ['PENDING', 'CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT'];

const COLUMNS = [
  { id: 'id', label: 'ID' },
  { id: 'guest', label: 'Guest' },
  { id: 'dates', label: 'Dates' },
  { id: 'status', label: 'Status' },
  { id: 'total', label: 'Total', align: 'right' },
  { id: 'actions', label: 'Actions', align: 'right' },
];

export default function AdminBookingsPage() {
  const { notifySuccess, notifyError } = useAdminUi();
  const [statusFilter, setStatusFilter] = useState('');
  const [searchId, setSearchId] = useState('');

  const fetcher = useCallback(
    (f) =>
      statusFilter
        ? bookingService.listByStatus(statusFilter, f)
        : bookingService.list(f),
    [statusFilter]
  );

  const {
    content,
    totalElements,
    filters,
    loading,
    error,
    setPage,
    setPageSize,
    reload,
  } = usePagedResource(fetcher, { size: 10 });

  const [detail, setDetail] = useState(null);
  const [statusDlg, setStatusDlg] = useState(null);
  const [nextStatus, setNextStatus] = useState('CONFIRMED');
  const [cancelTarget, setCancelTarget] = useState(null);
  const [busy, setBusy] = useState(false);

  const rows = content;

  const openDetail = async (id) => {
    try {
      const b = await bookingService.getById(id);
      setDetail(b);
    } catch (err) {
      notifyError(err.message || 'Load failed');
    }
  };

  const searchById = async () => {
    if (!searchId.trim()) return;
    openDetail(searchId.trim());
  };

  const applyStatus = async () => {
    setBusy(true);
    try {
      await bookingService.updateStatus(statusDlg.id, nextStatus);
      notifySuccess(`Booking #${statusDlg.id} → ${nextStatus}`);
      setStatusDlg(null);
      reload();
    } catch (err) {
      notifyError(err.message || 'Status update failed');
    } finally {
      setBusy(false);
    }
  };

  const cancelBooking = async () => {
    setBusy(true);
    try {
      await bookingService.cancel(cancelTarget.id);
      notifySuccess('Booking cancelled');
      setCancelTarget(null);
      reload();
    } catch (err) {
      notifyError(err.message || 'Cancel failed');
    } finally {
      setBusy(false);
    }
  };

  const timelineStep = useMemo(() => {
    if (!detail) return 0;
    if (detail.status === 'CANCELLED') return -1;
    const idx = TIMELINE.indexOf(detail.status);
    return idx < 0 ? 0 : idx;
  }, [detail]);

  return (
    <Box data-testid="admin-bookings-page">
      <AdminPageHeader
        title="Manage bookings"
        subtitle="List, filter by status, update lifecycle, cancel"
      />

      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} sx={{ mb: 2 }}>
        <TextField
          size="small"
          label="Booking ID"
          value={searchId}
          onChange={(e) => setSearchId(e.target.value)}
        />
        <Button variant="outlined" onClick={searchById}>Open</Button>
        <TextField
          size="small"
          select
          label="Status filter"
          value={statusFilter}
          onChange={(e) => {
            setStatusFilter(e.target.value);
            setPage(0);
          }}
          sx={{ minWidth: 180 }}
        >
          <MenuItem value="">All</MenuItem>
          {STATUSES.map((s) => (
            <MenuItem key={s} value={s}>{s}</MenuItem>
          ))}
        </TextField>
      </Stack>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <AdminDataTable
        columns={COLUMNS}
        rows={rows}
        loading={loading}
        emptyTitle="No bookings found"
        emptyDescription="Try another status filter or booking ID."
        page={filters.page || 0}
        rowsPerPage={filters.size || 10}
        totalElements={totalElements}
        onPageChange={setPage}
        onRowsPerPageChange={setPageSize}
        getRowSx={(b) =>
          b.status === 'CANCELLED'
            ? { bgcolor: 'rgba(211, 47, 47, 0.08)', '& td': { color: 'error.dark' } }
            : undefined
        }
        renderCell={(col, b) => {
          if (col.id === 'guest') return `${b.guestFirstName || ''} ${b.guestLastName || ''}`.trim();
          if (col.id === 'dates') {
            return `${formatDateLabel(b.checkInDate)} → ${formatDateLabel(b.checkOutDate)}`;
          }
          if (col.id === 'status') {
            return (
              <Chip
                size="small"
                label={b.status}
                color={b.status === 'CANCELLED' ? 'error' : 'default'}
                data-testid={b.status === 'CANCELLED' ? 'cancelled-booking-chip' : undefined}
              />
            );
          }
          if (col.id === 'total') return formatCurrency(b.totalAmount);
          if (col.id === 'actions') {
            return (
              <>
                <Button size="small" onClick={() => openDetail(b.id)}>Details</Button>
                <Button
                  size="small"
                  onClick={() => {
                    setStatusDlg(b);
                    setNextStatus('CONFIRMED');
                  }}
                >
                  Status
                </Button>
                <Button
                  size="small"
                  color="error"
                  disabled={b.status === 'CANCELLED' || b.status === 'CHECKED_OUT'}
                  onClick={() => setCancelTarget(b)}
                >
                  Cancel
                </Button>
              </>
            );
          }
          return b[col.id];
        }}
      />

      <Dialog open={Boolean(detail)} onClose={() => setDetail(null)} fullWidth maxWidth="sm">
        <DialogTitle>Booking #{detail?.id}</DialogTitle>
        <DialogContent>
          {detail && (
            <Stack spacing={2} sx={{ mt: 1 }}>
              <Typography>Guest: {detail.guestFirstName} {detail.guestLastName} ({detail.guestEmail})</Typography>
              <Typography>
                Stay: {formatDateLabel(detail.checkInDate)} → {formatDateLabel(detail.checkOutDate)} ({detail.numberOfNights} nights)
              </Typography>
              <Typography>Total: {formatCurrency(detail.totalAmount)}</Typography>
              <Typography>Status: {detail.status}</Typography>
              <Typography fontWeight={700}>Timeline</Typography>
              {timelineStep < 0 ? (
                <Alert severity="warning">Cancelled</Alert>
              ) : (
                <Stepper activeStep={timelineStep} alternativeLabel data-testid="booking-timeline">
                  {TIMELINE.map((label) => (
                    <Step key={label} completed={TIMELINE.indexOf(label) <= timelineStep}>
                      <StepLabel>{label}</StepLabel>
                    </Step>
                  ))}
                </Stepper>
              )}
              <Typography fontWeight={700}>Rooms</Typography>
              {(detail.rooms || []).map((r) => (
                <Typography key={r.id || r.roomId} variant="body2">
                  Room {r.roomNumber || r.roomId} · {formatCurrency(r.pricePerNight)}/night
                </Typography>
              ))}
            </Stack>
          )}
        </DialogContent>
      </Dialog>

      <Dialog open={Boolean(statusDlg)} onClose={() => setStatusDlg(null)}>
        <DialogTitle>Update status · #{statusDlg?.id}</DialogTitle>
        <DialogContent>
          <TextField
            select
            fullWidth
            label="New status"
            value={nextStatus}
            onChange={(e) => setNextStatus(e.target.value)}
            sx={{ mt: 1, minWidth: 240 }}
          >
            {STATUSES.filter((s) => s !== 'CANCELLED').map((s) => (
              <MenuItem key={s} value={s}>{s}</MenuItem>
            ))}
          </TextField>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setStatusDlg(null)}>Cancel</Button>
          <Button variant="contained" disabled={busy} onClick={applyStatus}>Update</Button>
        </DialogActions>
      </Dialog>

      <ConfirmDialog
        open={Boolean(cancelTarget)}
        title="Cancel booking?"
        message={`Cancel booking #${cancelTarget?.id}?`}
        confirmLabel="Cancel booking"
        loading={busy}
        onClose={() => setCancelTarget(null)}
        onConfirm={cancelBooking}
      />
    </Box>
  );
}
