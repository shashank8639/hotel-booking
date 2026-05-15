import { useCallback, useState } from 'react';
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
  TextField,
} from '@mui/material';
import AdminPageHeader from '../../components/admin/AdminPageHeader';
import AdminDataTable from '../../components/admin/AdminDataTable';
import { usePagedResource } from '../../hooks/usePagedResource';
import { useAdminUi } from '../../context/AdminUiContext';
import { paymentService } from '../../services/paymentService';
import { formatCurrency, formatDateLabel } from '../../utils/format';

const PAY_STATUSES = ['PENDING', 'SUCCESS', 'FAILED', 'REFUNDED', 'CANCELLED'];

const COLUMNS = [
  { id: 'id', label: 'ID' },
  { id: 'bookingId', label: 'Booking' },
  { id: 'status', label: 'Status' },
  { id: 'refund', label: 'Refund' },
  { id: 'amount', label: 'Amount', align: 'right' },
  { id: 'created', label: 'Created' },
  { id: 'actions', label: 'Actions', align: 'right' },
];

export default function AdminPaymentsPage() {
  const { notifySuccess, notifyError } = useAdminUi();
  const fetcher = useCallback((f) => paymentService.history(f), []);
  const {
    content,
    totalElements,
    filters,
    loading,
    error,
    setPage,
    setPageSize,
    patchFilters,
    reload,
  } = usePagedResource(fetcher, { size: 10 });

  const [refundDlg, setRefundDlg] = useState(null);
  const [refundAmount, setRefundAmount] = useState('');
  const [busy, setBusy] = useState(false);

  const downloadInvoice = async (bookingId) => {
    try {
      await paymentService.downloadInvoicePdf(bookingId);
      notifySuccess('Invoice download started');
    } catch (err) {
      notifyError(err.message || 'Invoice download failed');
    }
  };

  const submitRefund = async () => {
    setBusy(true);
    try {
      await paymentService.refund({
        paymentId: refundDlg.id,
        amount: refundAmount ? Number(refundAmount) : undefined,
      });
      notifySuccess('Refund processed');
      setRefundDlg(null);
      reload();
    } catch (err) {
      notifyError(err.message || 'Refund failed');
    } finally {
      setBusy(false);
    }
  };

  return (
    <Box data-testid="admin-payments-page">
      <AdminPageHeader
        title="Payment management"
        subtitle="History, filters, refunds, invoice PDF"
      />

      <Stack direction={{ xs: 'column', md: 'row' }} spacing={1} sx={{ mb: 2 }}>
        <TextField
          size="small"
          label="Booking ID"
          value={filters.bookingId || ''}
          onChange={(e) => patchFilters({ bookingId: e.target.value || undefined })}
        />
        <TextField
          size="small"
          select
          label="Status"
          value={filters.status || ''}
          onChange={(e) => patchFilters({ status: e.target.value || undefined })}
          sx={{ minWidth: 180 }}
        >
          <MenuItem value="">All</MenuItem>
          {PAY_STATUSES.map((s) => (
            <MenuItem key={s} value={s}>{s}</MenuItem>
          ))}
        </TextField>
        <TextField
          size="small"
          type="date"
          label="From"
          InputLabelProps={{ shrink: true }}
          value={filters.fromDate || ''}
          onChange={(e) => patchFilters({ fromDate: e.target.value || undefined })}
        />
        <TextField
          size="small"
          type="date"
          label="To"
          InputLabelProps={{ shrink: true }}
          value={filters.toDate || ''}
          onChange={(e) => patchFilters({ toDate: e.target.value || undefined })}
        />
      </Stack>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <AdminDataTable
        columns={COLUMNS}
        rows={content}
        loading={loading}
        emptyTitle="No payments found"
        emptyDescription="Adjust status or date filters."
        page={filters.page || 0}
        rowsPerPage={filters.size || 10}
        totalElements={totalElements}
        onPageChange={setPage}
        onRowsPerPageChange={setPageSize}
        renderCell={(col, p) => {
          if (col.id === 'status') return <Chip size="small" label={p.status} />;
          if (col.id === 'refund') {
            return p.refundedAmount != null ? formatCurrency(p.refundedAmount || 0) : '—';
          }
          if (col.id === 'amount') return formatCurrency(p.amount);
          if (col.id === 'created') {
            return p.createdAt ? formatDateLabel(String(p.createdAt).slice(0, 10)) : '—';
          }
          if (col.id === 'actions') {
            return (
              <>
                <Button size="small" onClick={() => downloadInvoice(p.bookingId)}>Invoice</Button>
                <Button
                  size="small"
                  disabled={p.status !== 'SUCCESS'}
                  onClick={() => {
                    setRefundDlg(p);
                    setRefundAmount('');
                  }}
                >
                  Refund
                </Button>
              </>
            );
          }
          return p[col.id];
        }}
      />

      <Dialog open={Boolean(refundDlg)} onClose={() => setRefundDlg(null)}>
        <DialogTitle>Refund payment #{refundDlg?.id}</DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            sx={{ mt: 1 }}
            label="Amount (blank = full)"
            type="number"
            value={refundAmount}
            onChange={(e) => setRefundAmount(e.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRefundDlg(null)}>Cancel</Button>
          <Button variant="contained" color="warning" disabled={busy} onClick={submitRefund}>
            Refund
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
