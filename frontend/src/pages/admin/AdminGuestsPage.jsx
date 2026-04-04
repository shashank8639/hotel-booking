import { useCallback, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Dialog,
  DialogContent,
  DialogTitle,
  IconButton,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import VisibilityIcon from '@mui/icons-material/Visibility';
import HistoryIcon from '@mui/icons-material/History';
import AddIcon from '@mui/icons-material/Add';
import AdminPageHeader from '../../components/admin/AdminPageHeader';
import ConfirmDialog from '../../components/admin/ConfirmDialog';
import GuestFormDialog from '../../components/admin/GuestFormDialog';
import AdminDataTable from '../../components/admin/AdminDataTable';
import { usePagedResource } from '../../hooks/usePagedResource';
import { useAdminUi } from '../../context/AdminUiContext';
import { guestService } from '../../services/guestService';
import { bookingService } from '../../services/bookingService';
import { formatCurrency, formatDateLabel } from '../../utils/format';

const COLUMNS = [
  { id: 'name', label: 'Name' },
  { id: 'email', label: 'Email' },
  { id: 'phone', label: 'Phone' },
  { id: 'actions', label: 'Actions', align: 'right' },
];

export default function AdminGuestsPage() {
  const { notifySuccess, notifyError } = useAdminUi();
  const [q, setQ] = useState('');
  const [searchRows, setSearchRows] = useState(null);
  const fetcher = useCallback((f) => guestService.list(f), []);
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

  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [viewing, setViewing] = useState(null);
  const [history, setHistory] = useState(null);
  const [confirm, setConfirm] = useState(null);
  const [busy, setBusy] = useState(false);

  const rows = searchRows ?? content;

  const runSearch = async () => {
    if (!q.trim()) {
      setSearchRows(null);
      return;
    }
    try {
      if (q.includes('@')) {
        const one = await guestService.searchByEmail(q.trim());
        setSearchRows([one]);
      } else if (/^\+?[0-9]/.test(q.trim())) {
        const one = await guestService.searchByPhone(q.trim());
        setSearchRows([one]);
      } else {
        const list = await guestService.searchByName(q.trim());
        setSearchRows(list || []);
      }
    } catch (err) {
      notifyError(err.message || 'Search failed');
      setSearchRows([]);
    }
  };

  const save = async (values) => {
    setBusy(true);
    try {
      if (editing) {
        await guestService.update(editing.id, values);
        notifySuccess('Guest updated');
      } else {
        await guestService.create(values);
        notifySuccess('Guest created');
      }
      setFormOpen(false);
      setEditing(null);
      setSearchRows(null);
      reload();
    } catch (err) {
      notifyError(err.message || 'Save failed');
    } finally {
      setBusy(false);
    }
  };

  const remove = async () => {
    setBusy(true);
    try {
      await guestService.remove(confirm.id);
      notifySuccess('Guest deleted');
      setConfirm(null);
      reload();
    } catch (err) {
      notifyError(err.message || 'Delete failed — guest may have bookings');
    } finally {
      setBusy(false);
    }
  };

  const loadHistory = async (guest) => {
    try {
      const page = await bookingService.listByGuest(guest.id, { size: 20 });
      setHistory({ guest, bookings: page.content || page || [] });
    } catch (err) {
      notifyError(err.message || 'Could not load booking history');
    }
  };

  return (
    <Box data-testid="admin-guests-page">
      <AdminPageHeader
        title="Manage guests"
        subtitle="List, search, edit, delete, and booking history"
        actions={
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => {
              setEditing(null);
              setFormOpen(true);
            }}
          >
            Add guest
          </Button>
        }
      />

      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} sx={{ mb: 2 }}>
        <TextField
          size="small"
          label="Search name / email / phone"
          value={q}
          onChange={(e) => setQ(e.target.value)}
          fullWidth
        />
        <Button variant="outlined" onClick={runSearch}>Search</Button>
        <Button
          onClick={() => {
            setQ('');
            setSearchRows(null);
          }}
        >
          Clear
        </Button>
      </Stack>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <AdminDataTable
        columns={COLUMNS}
        rows={rows}
        loading={loading && !searchRows}
        emptyTitle="No guests found"
        emptyDescription="Add a guest or clear your search."
        page={filters.page || 0}
        rowsPerPage={filters.size || 10}
        totalElements={searchRows ? rows.length : totalElements}
        onPageChange={searchRows ? undefined : setPage}
        onRowsPerPageChange={searchRows ? undefined : setPageSize}
        renderCell={(col, g) => {
          if (col.id === 'name') return `${g.firstName} ${g.lastName}`;
          if (col.id === 'phone') return g.phone || '—';
          if (col.id === 'actions') {
            return (
              <>
                <IconButton size="small" onClick={() => setViewing(g)}><VisibilityIcon /></IconButton>
                <IconButton size="small" onClick={() => loadHistory(g)}><HistoryIcon /></IconButton>
                <IconButton
                  size="small"
                  onClick={() => {
                    setEditing(g);
                    setFormOpen(true);
                  }}
                >
                  <EditIcon />
                </IconButton>
                <IconButton size="small" color="error" onClick={() => setConfirm(g)}>
                  <DeleteIcon />
                </IconButton>
              </>
            );
          }
          return g[col.id];
        }}
      />

      <GuestFormDialog
        open={formOpen}
        guest={editing}
        loading={busy}
        onClose={() => setFormOpen(false)}
        onSubmit={save}
      />
      <ConfirmDialog
        open={Boolean(confirm)}
        title="Delete guest?"
        message={`Delete ${confirm?.firstName} ${confirm?.lastName}?`}
        loading={busy}
        onClose={() => setConfirm(null)}
        onConfirm={remove}
      />

      <Dialog open={Boolean(viewing)} onClose={() => setViewing(null)} fullWidth maxWidth="xs">
        <DialogTitle>Guest details</DialogTitle>
        <DialogContent>
          {viewing && (
            <Stack spacing={1} sx={{ mt: 1 }}>
              <Typography>Name: {viewing.firstName} {viewing.lastName}</Typography>
              <Typography>Email: {viewing.email}</Typography>
              <Typography>Phone: {viewing.phone || '—'}</Typography>
              <Typography>ID: {viewing.id}</Typography>
            </Stack>
          )}
        </DialogContent>
      </Dialog>

      <Dialog open={Boolean(history)} onClose={() => setHistory(null)} fullWidth maxWidth="md">
        <DialogTitle>
          Booking history · {history?.guest?.firstName} {history?.guest?.lastName}
        </DialogTitle>
        <DialogContent>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>Check-in</TableCell>
                <TableCell>Status</TableCell>
                <TableCell align="right">Total</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {(history?.bookings || []).map((b) => (
                <TableRow key={b.id}>
                  <TableCell>{b.id}</TableCell>
                  <TableCell>{formatDateLabel(b.checkInDate)}</TableCell>
                  <TableCell>{b.status}</TableCell>
                  <TableCell align="right">{formatCurrency(b.totalAmount)}</TableCell>
                </TableRow>
              ))}
              {!history?.bookings?.length && (
                <TableRow>
                  <TableCell colSpan={4}>No bookings</TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </DialogContent>
      </Dialog>
    </Box>
  );
}
