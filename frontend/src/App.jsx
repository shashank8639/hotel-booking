import { AuthProvider } from './context/AuthContext';
import AppRoutes from './routes/AppRoutes';

/**
 * Root app shell — AuthProvider wraps all routes so guards/hooks share one session.
 */
function App() {
  return (
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  );
}

export default App;
