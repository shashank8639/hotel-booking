import {
  Box,
  CircularProgress,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TablePagination,
  TableRow,
  TableSortLabel,
} from '@mui/material';
import AdminEmptyState from './AdminEmptyState';

/**
 * Shared admin table: headers (optional sort), rows, empty state, Spring Page pagination.
 *
 * columns: [{ id, label, align?, sortable? }]
 * sort: Spring-style "field,asc" | "field,desc"
 */
export default function AdminDataTable({
  columns = [],
  rows = [],
  getRowId = (row) => row.id,
  renderCell,
  getRowSx,
  loading = false,
  emptyTitle,
  emptyDescription,
  page = 0,
  rowsPerPage = 10,
  totalElements = 0,
  onPageChange,
  onRowsPerPageChange,
  sort,
  onSortChange,
  size = 'small',
  'data-testid': testId = 'admin-data-table',
}) {
  const [sortField, sortDir] = parseSort(sort);

  if (loading) {
    return (
      <Box sx={{ py: 4, textAlign: 'center' }} data-testid={testId}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box data-testid={testId}>
      <Table size={size}>
        <TableHead>
          <TableRow>
            {columns.map((col) => (
              <TableCell
                key={col.id}
                align={col.align || 'left'}
                sortDirection={sortField === col.id ? sortDir : false}
              >
                {col.sortable && onSortChange ? (
                  <TableSortLabel
                    active={sortField === col.id}
                    direction={sortField === col.id ? sortDir : 'asc'}
                    onClick={() => onSortChange(toggleSort(sortField, sortDir, col.id))}
                  >
                    {col.label}
                  </TableSortLabel>
                ) : (
                  col.label
                )}
              </TableCell>
            ))}
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.length === 0 ? (
            <TableRow>
              <TableCell colSpan={columns.length} sx={{ border: 0 }}>
                <AdminEmptyState title={emptyTitle} description={emptyDescription} />
              </TableCell>
            </TableRow>
          ) : (
            rows.map((row) => (
              <TableRow key={getRowId(row)} hover sx={getRowSx?.(row)}>
                {columns.map((col) => (
                  <TableCell key={col.id} align={col.align || 'left'}>
                    {renderCell ? renderCell(col, row) : row[col.id]}
                  </TableCell>
                ))}
              </TableRow>
            ))
          )}
        </TableBody>
      </Table>
      {onPageChange && (
        <TablePagination
          component="div"
          count={totalElements}
          page={page}
          onPageChange={(_, p) => onPageChange(p)}
          rowsPerPage={rowsPerPage}
          onRowsPerPageChange={(e) => onRowsPerPageChange?.(parseInt(e.target.value, 10))}
        />
      )}
    </Box>
  );
}

export function parseSort(sort) {
  if (!sort || typeof sort !== 'string') return [null, 'asc'];
  const [field, dir] = sort.split(',');
  return [field || null, dir === 'desc' ? 'desc' : 'asc'];
}

export function toggleSort(currentField, currentDir, nextField) {
  if (currentField === nextField) {
    return `${nextField},${currentDir === 'asc' ? 'desc' : 'asc'}`;
  }
  return `${nextField},asc`;
}
