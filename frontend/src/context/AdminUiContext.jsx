import { createContext, useCallback, useContext, useMemo, useState } from 'react';
import { Alert, Snackbar } from '@mui/material';

const AdminUiContext = createContext(null);

/**
 * Shared snackbars for admin CRUD feedback (success / error).
 */
export function AdminUiProvider({ children }) {
  const [snack, setSnack] = useState({ open: false, message: '', severity: 'info' });

  const notify = useCallback((message, severity = 'info') => {
    setSnack({ open: true, message, severity });
  }, []);

  const notifySuccess = useCallback((message) => notify(message, 'success'), [notify]);
  const notifyError = useCallback((message) => notify(message, 'error'), [notify]);

  const value = useMemo(
    () => ({ notify, notifySuccess, notifyError }),
    [notify, notifySuccess, notifyError]
  );

  return (
    <AdminUiContext.Provider value={value}>
      {children}
      <Snackbar
        open={snack.open}
        autoHideDuration={4000}
        onClose={() => setSnack((s) => ({ ...s, open: false }))}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      >
        <Alert
          severity={snack.severity}
          variant="filled"
          onClose={() => setSnack((s) => ({ ...s, open: false }))}
          sx={{ width: '100%' }}
        >
          {snack.message}
        </Alert>
      </Snackbar>
    </AdminUiContext.Provider>
  );
}

export function useAdminUi() {
  const ctx = useContext(AdminUiContext);
  if (!ctx) {
    throw new Error('useAdminUi must be used within AdminUiProvider');
  }
  return ctx;
}
