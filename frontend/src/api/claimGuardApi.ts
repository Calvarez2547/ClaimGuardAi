import { apiRequest } from './http';
import type {
  AdminUser,
  AuditEventItem,
  Claim,
  ClaimAnalysis,
  ClaimStatus,
  ClaimSummary,
  CreateClaimPayload,
  CurrentUser,
  DashboardSummary,
  LoginResponse,
  PageResponse,
  ReviewNote,
} from '../types/api';

export const claimGuardApi = {
  login(username: string, password: string) {
    return apiRequest<LoginResponse>('/api/auth/login', {
      method: 'POST',
      auth: false,
      body: { username, password },
    });
  },

  register(username: string, email: string, password: string) {
    return apiRequest<LoginResponse>('/api/auth/register', {
      method: 'POST',
      auth: false,
      body: { username, email, password },
    });
  },

  refreshToken(refreshToken: string) {
    return apiRequest<LoginResponse>('/api/auth/refresh', {
      method: 'POST',
      auth: false,
      body: { refreshToken },
    });
  },

  logout(refreshToken: string) {
    return apiRequest<void>('/api/auth/logout', {
      method: 'POST',
      body: { refreshToken },
    });
  },

  me() {
    return apiRequest<CurrentUser>('/api/auth/me');
  },

  listClaims() {
    return apiRequest<ClaimSummary[]>('/api/claims');
  },

  createClaim(payload: CreateClaimPayload) {
    return apiRequest<Claim>('/api/claims', {
      method: 'POST',
      body: payload,
    });
  },

  getClaim(claimId: number) {
    return apiRequest<Claim>(`/api/claims/${claimId}`);
  },

  updateClaimStatus(claimId: number, status: ClaimStatus) {
    return apiRequest<Claim>(`/api/claims/${claimId}/status`, {
      method: 'PATCH',
      body: { status },
    });
  },

  listReviewNotes(claimId: number) {
    return apiRequest<ReviewNote[]>(`/api/claims/${claimId}/review-notes`);
  },

  addReviewNote(claimId: number, noteText: string) {
    return apiRequest<ReviewNote>(`/api/claims/${claimId}/review-notes`, {
      method: 'POST',
      body: { noteText },
    });
  },

  analyzeClaim(claimId: number) {
    return apiRequest<ClaimAnalysis>(`/api/claims/${claimId}/analyze`, {
      method: 'POST',
    });
  },

  getLatestAnalysis(claimId: number) {
    return apiRequest<ClaimAnalysis>(`/api/claims/${claimId}/analysis/latest`);
  },

  getAnalysisHistory(claimId: number) {
    return apiRequest<ClaimAnalysis[]>(`/api/claims/${claimId}/analysis/history`);
  },

  getDashboardSummary() {
    return apiRequest<DashboardSummary>('/api/dashboard/summary');
  },

  getAuditEvents(page = 0, size = 50, eventType?: string) {
    const params = new URLSearchParams({ page: String(page), size: String(size) });
    if (eventType) params.set('eventType', eventType);
    return apiRequest<PageResponse<AuditEventItem>>(`/api/audit/events?${params}`);
  },

  getMyAuditEvents(page = 0, size = 25) {
    return apiRequest<PageResponse<AuditEventItem>>(
      `/api/audit/events/me?page=${page}&size=${size}`,
    );
  },

  adminListUsers(page = 0, size = 25) {
    return apiRequest<PageResponse<AdminUser>>(`/api/admin/users?page=${page}&size=${size}`);
  },

  adminGetUser(userId: number) {
    return apiRequest<AdminUser>(`/api/admin/users/${userId}`);
  },

  adminUpdateRoles(userId: number, roles: string[]) {
    return apiRequest<AdminUser>(`/api/admin/users/${userId}/roles`, {
      method: 'PATCH',
      body: { roles },
    });
  },

  adminToggleEnabled(userId: number, enabled: boolean) {
    return apiRequest<AdminUser>(`/api/admin/users/${userId}/enabled`, {
      method: 'PATCH',
      body: { enabled },
    });
  },
};
