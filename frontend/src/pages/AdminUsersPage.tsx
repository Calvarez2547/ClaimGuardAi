import { useEffect, useState } from 'react';
import { Users } from 'lucide-react';
import { claimGuardApi } from '../api/claimGuardApi';
import { Badge } from '../components/Badge';
import { ErrorState, LoadingState } from '../components/State';
import { friendlyError } from '../utils/errors';
import type { AdminUser, PageResponse } from '../types/api';

export function AdminUsersPage() {
  const [data, setData] = useState<PageResponse<AdminUser> | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [togglingId, setTogglingId] = useState<number | null>(null);

  function load(p: number) {
    setLoading(true);
    setError('');
    claimGuardApi
      .adminListUsers(p)
      .then(setData)
      .catch((e) => setError(friendlyError(e)))
      .finally(() => setLoading(false));
  }

  useEffect(() => { load(page); }, [page]);

  async function handleToggleEnabled(userId: number, currentlyEnabled: boolean) {
    setTogglingId(userId);
    try {
      await claimGuardApi.adminToggleEnabled(userId, !currentlyEnabled);
      load(page);
    } catch (e) {
      setError(friendlyError(e));
    } finally {
      setTogglingId(null);
    }
  }

  if (loading) return <LoadingState label="Loading users..." />;
  if (error) return <ErrorState message={error} />;
  if (!data) return null;

  return (
    <div className="p-6 max-w-7xl mx-auto">
      <div className="flex items-center gap-3 mb-6">
        <Users size={22} className="text-app-muted" />
        <h1 className="text-xl font-bold text-app-text">User Management</h1>
        <span className="ml-auto text-sm text-app-muted">
          {data.totalElements.toLocaleString()} users
        </span>
      </div>

      <div className="bg-app-panel rounded-2xl border border-app-border shadow-card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-app-border bg-app-panel-soft">
                <th className="px-4 py-3 text-left font-semibold text-app-muted">User</th>
                <th className="px-4 py-3 text-left font-semibold text-app-muted">Roles</th>
                <th className="px-4 py-3 text-left font-semibold text-app-muted">Status</th>
                <th className="px-4 py-3 text-left font-semibold text-app-muted">Joined</th>
                <th className="px-4 py-3 text-left font-semibold text-app-muted">Actions</th>
              </tr>
            </thead>
            <tbody>
              {data.content.length === 0 && (
                <tr>
                  <td colSpan={5} className="px-4 py-8 text-center text-app-muted">
                    No users found.
                  </td>
                </tr>
              )}
              {data.content.map((user) => (
                <tr
                  key={user.id}
                  className="border-b border-app-border last:border-0 hover:bg-app-panel-soft transition-colors"
                >
                  <td className="px-4 py-3 align-middle">
                    <div className="font-medium text-app-text">{user.username}</div>
                    <div className="text-xs text-app-muted">{user.email}</div>
                  </td>
                  <td className="px-4 py-3 align-middle">
                    <div className="flex flex-wrap gap-1">
                      {[...user.roles].map((r) => (
                        <Badge key={r} tone="neutral">{r.replace(/_/g, ' ')}</Badge>
                      ))}
                    </div>
                  </td>
                  <td className="px-4 py-3 align-middle">
                    <Badge tone={user.enabled ? 'success' : 'danger'}>
                      {user.enabled ? 'Active' : 'Disabled'}
                    </Badge>
                  </td>
                  <td className="px-4 py-3 align-middle text-app-muted whitespace-nowrap">
                    {new Date(user.createdAt).toLocaleDateString()}
                  </td>
                  <td className="px-4 py-3 align-middle">
                    <button
                      disabled={togglingId === user.id}
                      onClick={() => handleToggleEnabled(user.id, user.enabled)}
                      className={`px-3 py-1.5 rounded-lg text-xs font-semibold border transition-colors disabled:opacity-50 ${
                        user.enabled
                          ? 'border-c-red text-c-red hover:bg-c-red-soft'
                          : 'border-c-green text-c-green hover:bg-c-green-soft'
                      }`}
                    >
                      {user.enabled ? 'Disable' : 'Enable'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {data.totalPages > 1 && (
          <div className="flex items-center justify-between px-4 py-3 border-t border-app-border bg-app-panel-soft">
            <button
              disabled={page === 0}
              onClick={() => setPage((p) => p - 1)}
              className="px-4 py-2 rounded-lg text-sm font-medium border border-app-border disabled:opacity-40 hover:bg-app-bg transition-colors"
            >
              Previous
            </button>
            <span className="text-sm text-app-muted">
              Page {page + 1} of {data.totalPages}
            </span>
            <button
              disabled={page + 1 >= data.totalPages}
              onClick={() => setPage((p) => p + 1)}
              className="px-4 py-2 rounded-lg text-sm font-medium border border-app-border disabled:opacity-40 hover:bg-app-bg transition-colors"
            >
              Next
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
