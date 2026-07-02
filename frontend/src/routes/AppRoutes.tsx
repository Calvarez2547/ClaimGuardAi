import { Navigate, Route, Routes } from 'react-router-dom';
import { AppLayout } from '../layouts/AppLayout';
import { ProtectedRoute } from './ProtectedRoute';
import { AnalysisHistoryPage } from '../pages/AnalysisHistoryPage';
import { AuditLogPage } from '../pages/AuditLogPage';
import { AdminUsersPage } from '../pages/AdminUsersPage';
import { ClaimDetailPage } from '../pages/ClaimDetailPage';
import { ClaimsListPage } from '../pages/ClaimsListPage';
import { CreateClaimPage } from '../pages/CreateClaimPage';
import { DashboardPage } from '../pages/DashboardPage';
import { LoginPage } from '../pages/LoginPage';
import { NotFoundPage } from '../pages/NotFoundPage';
import { PlaceholderPage } from '../pages/PlaceholderPage';
import { RegisterPage } from '../pages/RegisterPage';

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route index element={<DashboardPage />} />
          <Route path="claims" element={<ClaimsListPage />} />
          <Route path="claims/new" element={<CreateClaimPage />} />
          <Route path="claims/:claimId" element={<ClaimDetailPage />} />
          <Route path="analysis-history" element={<AnalysisHistoryPage />} />
          <Route path="audit" element={<AuditLogPage />} />
          <Route path="admin/users" element={<AdminUsersPage />} />
          <Route path="reports" element={<PlaceholderPage kind="reports" />} />
          <Route path="settings" element={<PlaceholderPage kind="settings" />} />
        </Route>
      </Route>
      <Route path="/404" element={<NotFoundPage />} />
      <Route path="*" element={<Navigate to="/404" replace />} />
    </Routes>
  );
}
