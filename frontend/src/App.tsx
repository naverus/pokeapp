import { Navigate, Route, Routes } from 'react-router-dom';
import { AppLayout } from './shared/components/AppLayout';
import { BrowsePage } from './features/catalog/BrowsePage';
import { DetailPage } from './features/catalog/DetailPage';
import { CollectionPage } from './features/collection/CollectionPage';
import { LoginPage } from './features/auth/LoginPage';
import { RegisterPage } from './features/auth/RegisterPage';
import { ProtectedRoute } from './features/auth/ProtectedRoute';

export function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route index element={<BrowsePage />} />
        <Route path="pokemon/:idOrName" element={<DetailPage />} />
        <Route
          path="collection"
          element={
            <ProtectedRoute>
              <CollectionPage />
            </ProtectedRoute>
          }
        />
        <Route path="login" element={<LoginPage />} />
        <Route path="register" element={<RegisterPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}
