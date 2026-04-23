import { useCallback, useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Chip,
  IconButton,
  MenuItem,
  Stack,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import ImageIcon from '@mui/icons-material/Image';
import ToggleOnIcon from '@mui/icons-material/ToggleOn';
import AdminPageHeader from '../../components/admin/AdminPageHeader';
import ConfirmDialog from '../../components/admin/ConfirmDialog';
import RoomFormDialog from '../../components/admin/RoomFormDialog';
import AdminDataTable from '../../components/admin/AdminDataTable';
import { usePagedResource } from '../../hooks/usePagedResource';
import { useDebouncedValue } from '../../hooks/useDebouncedValue';
import { useAdminUi } from '../../context/AdminUiContext';
import { roomService } from '../../services/roomService';
import { adminRoomService } from '../../services/adminRoomService';
import { formatCurrency } from '../../utils/format';

const COLUMNS = [
  { id: 'roomNumber', label: 'Number', sortable: true },
  { id: 'roomType', label: 'Type', sortable: true },
  { id: 'status', label: 'Status', sortable: true },
  { id: 'capacity', label: 'Capacity', sortable: true },
  { id: 'price', label: 'Price', sortable: true },
  { id: 'actions', label: 'Actions', align: 'right' },
];

export default function AdminRoomsPage() {
  const { notifySuccess, notifyError } = useAdminUi();
  const fetcher = useCallback((f) => roomService.search(f), []);
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
  } = usePagedResource(fetcher, { size: 10, sort: 'roomNumber,asc' });

  const [roomNumberInput, setRoomNumberInput] = useState(filters.roomNumber || '');
  const debouncedRoomNumber = useDebouncedValue(roomNumberInput, 400);

  useEffect(() => {
    const next = debouncedRoomNumber || undefined;
    if ((filters.roomNumber || undefined) !== next) {
      patchFilters({ roomNumber: next });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debouncedRoomNumber]);

  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [confirm, setConfirm] = useState(null);
  const [preview, setPreview] = useState(null);
  const [busy, setBusy] = useState(false);

  const submitRoom = async (values) => {
    setBusy(true);
    try {
      const payload = {
        ...values,
        floorNumber: Number(values.floorNumber),
        capacity: Number(values.capacity),
        pricePerNight: Number(values.pricePerNight),
        discountedPrice: values.discountedPrice === '' ? null : Number(values.discountedPrice),
      };
      if (editing) {
        await adminRoomService.update(editing.id, payload);
        notifySuccess('Room updated');
      } else {
        await adminRoomService.create(payload);
        notifySuccess('Room created');
      }
      setFormOpen(false);
      setEditing(null);
      reload();
    } catch (err) {
      notifyError(err.message || 'Save failed');
    } finally {
      setBusy(false);
    }
  };

  const toggleAvailability = async (room) => {
    const next = room.status === 'AVAILABLE' ? 'MAINTENANCE' : 'AVAILABLE';
    try {
      await adminRoomService.updateAvailability(room.id, next);
      notifySuccess(`Room ${room.roomNumber} → ${next}`);
      reload();
    } catch (err) {
      notifyError(err.message || 'Availability update failed');
    }
  };

  const deleteRoom = async () => {
    if (!confirm) return;
    setBusy(true);
    try {
      await adminRoomService.remove(confirm.id);
      notifySuccess('Room deleted');
      setConfirm(null);
      reload();
    } catch (err) {
      notifyError(err.message || 'Delete failed');
    } finally {
      setBusy(false);
    }
  };

  const openImages = async (room) => {
    try {
      const images = await roomService.getImages(room.id);
      setPreview({ room, images: images || [] });
    } catch (err) {
      notifyError(err.message || 'Could not load images');
    }
  };

  const handleSort = (sort) => {
    // Map UI "price" column to API field
    const normalized = sort.startsWith('price,')
      ? sort.replace(/^price,/, 'pricePerNight,')
      : sort;
    patchFilters({ sort: normalized });
  };

  const apiSort =
    filters.sort?.startsWith('pricePerNight,')
      ? filters.sort.replace(/^pricePerNight,/, 'price,')
      : filters.sort;

  return (
    <Box data-testid="admin-rooms-page">
      <AdminPageHeader
        title="Manage rooms"
        subtitle="Search (debounced), sort, filter, CRUD via /rooms and /admin/rooms"
        actions={
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => {
              setEditing(null);
              setFormOpen(true);
            }}
          >
            Create room
          </Button>
        }
      />

      <Stack direction={{ xs: 'column', md: 'row' }} spacing={1} sx={{ mb: 2 }}>
        <TextField
          size="small"
          label="Search room #"
          value={roomNumberInput}
          onChange={(e) => setRoomNumberInput(e.target.value)}
          helperText="Debounced 400ms"
          data-testid="room-search-input"
        />
        <TextField
          size="small"
          select
          label="Type"
          value={filters.roomType || ''}
          onChange={(e) => patchFilters({ roomType: e.target.value || undefined })}
          sx={{ minWidth: 140 }}
        >
          <MenuItem value="">All</MenuItem>
          {['STANDARD', 'DELUXE', 'EXECUTIVE', 'SUITE', 'FAMILY', 'PRESIDENTIAL'].map((t) => (
            <MenuItem key={t} value={t}>{t}</MenuItem>
          ))}
        </TextField>
        <TextField
          size="small"
          select
          label="Status"
          value={filters.status || ''}
          onChange={(e) => patchFilters({ status: e.target.value || undefined })}
          sx={{ minWidth: 160 }}
        >
          <MenuItem value="">All</MenuItem>
          {['AVAILABLE', 'RESERVED', 'OCCUPIED', 'MAINTENANCE', 'OUT_OF_SERVICE'].map((t) => (
            <MenuItem key={t} value={t}>{t}</MenuItem>
          ))}
        </TextField>
      </Stack>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <AdminDataTable
        columns={COLUMNS}
        rows={content}
        loading={loading}
        emptyTitle="No rooms found"
        emptyDescription="Create a room or clear filters."
        page={filters.page || 0}
        rowsPerPage={filters.size || 10}
        totalElements={totalElements}
        onPageChange={setPage}
        onRowsPerPageChange={setPageSize}
        sort={apiSort}
        onSortChange={handleSort}
        renderCell={(col, room) => {
          if (col.id === 'status') return <Chip size="small" label={room.status} />;
          if (col.id === 'price') {
            return formatCurrency(room.effectivePrice ?? room.pricePerNight);
          }
          if (col.id === 'actions') {
            return (
              <>
                <Tooltip title="Images">
                  <IconButton size="small" onClick={() => openImages(room)}><ImageIcon /></IconButton>
                </Tooltip>
                <Tooltip title="Toggle availability">
                  <IconButton size="small" onClick={() => toggleAvailability(room)}><ToggleOnIcon /></IconButton>
                </Tooltip>
                <Tooltip title="Edit">
                  <IconButton
                    size="small"
                    onClick={() => {
                      setEditing(room);
                      setFormOpen(true);
                    }}
                  >
                    <EditIcon />
                  </IconButton>
                </Tooltip>
                <Tooltip title="Delete">
                  <IconButton size="small" color="error" onClick={() => setConfirm(room)}>
                    <DeleteIcon />
                  </IconButton>
                </Tooltip>
              </>
            );
          }
          return room[col.id];
        }}
      />

      <RoomFormDialog
        open={formOpen}
        room={editing}
        loading={busy}
        onClose={() => setFormOpen(false)}
        onSubmit={submitRoom}
      />
      <ConfirmDialog
        open={Boolean(confirm)}
        title="Delete room?"
        message={`Delete room ${confirm?.roomNumber}? This cannot be undone.`}
        confirmLabel="Delete"
        loading={busy}
        onClose={() => setConfirm(null)}
        onConfirm={deleteRoom}
      />

      {preview && (
        <Alert
          severity="info"
          onClose={() => setPreview(null)}
          sx={{ mt: 2 }}
          data-testid="image-preview"
        >
          <Typography fontWeight={700}>Images · Room {preview.room.roomNumber}</Typography>
          {preview.images.length ? (
            preview.images.map((img) => (
              <Typography key={img.id} variant="body2">
                {img.imageUrl || img.url || `Image #${img.id}`}
              </Typography>
            ))
          ) : (
            <Typography variant="body2">No images uploaded</Typography>
          )}
        </Alert>
      )}
    </Box>
  );
}
