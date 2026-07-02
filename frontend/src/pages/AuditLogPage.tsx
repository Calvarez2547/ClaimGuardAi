import { useEffect, useState } from 'react';
import { ShieldAlert } from 'lucide-react';
import { claimGuardApi } from '../api/claimGuardApi';
import { useAuth } from '../auth/AuthContext';
import { ErrorState, LoadingState } from '../components/State';
import { friendlyError } from '../utils/errors';
import type { AuditEventItem, PageResponse } from '../types/api';

const EVENT_TYPE_OPTIONS = [
  '',
  'LOGIN_SUCCESS',
  'LOGIN_FAILURE',
  'LOGOUT',
  'REGISTER',
  'TOKEN_REFRESHED',
  'CLAIM_CREATED',
  'CLAIM_STATUS_UPDATED',
  'REVIEW_NOTE_ADDED',
  'ANALYSIS_RUN',
  'UNAUTHORIZED_ACCESS',
];

function eventTypeBadge(type: string) {
  const map: Record<string, string> = {
    LOGIN_SUCCESS: 'bg-c-green-soft text-c-green',
    LOGIN_FAILURE: 'bg-c-red-soft text-c-red',
    LOGOUT: 'bg-app-panel-soft text-app-muted',
    REGISTER: 'bg-primary-soft text-primary',
    UNAUTHORIZED_ACCESS: 'bg-c-red-soft text-c-red',
    ANALYSIS_RUN: 'bg-c-teal-soft text-c-teal',
  };
  const cls = map[type] ?? 'bg-c-orange-soft text-c-orange';
  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-bold ${cls}`}>
      {type.replace(/_/g, ' ')}
    </span>
  );
}

export function AuditLogPage() {
  const { hasRole } = useAuth();
  const isAdmin = hasRole('ADMINISTRATOR');

  const [data, setData] = useState<PageResponse<AuditEventItem> | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [eventType, setEventType] = useState('');

  useEffect(() => {
    setLoading(true);
    setError('');
    const fetch = isAdmin
      ? claimGuardApi.getAuditEvents(page, 50, eventType || undefined)
      : claimGuardApi.getMyAuditEvents(page, 25);

    fetch
      .then(setData)
      .catch((e) => setError(friendlyError(e)))
      .finally(() => setLoading(false));
  }, [isAdmin, page, eventType]);

  if (loading) return <LoadingState label="Loading audit log..." />;
  if (error) return <ErrorState message={error} />;
  if (!data) return null;

  return (
    <div className="p-6 max-w-7xl mx-auto">
      <div className="flex items-center gap-3 mb-6">
        <ShieldAlert size={22} className="text-app-muted" />
        <h1 className="text-xl font-bold text-app-text">Audit Log</h1>
        <span className="ml-auto text-sm text-app-muted">
          {data.totalElements.toLocaleString()} events
        </span>
      </div>

      {isAdmin && (
        <div className="mb-4 flex items-center gap-3">
          <label className="text-sm font-medium text-app-muted">Filter by type</label>
          <select
            value={eventType}
            onChange={(e) => { setEventType(e.target.value); setPage(0); }}
            className="border border-app-border rounded-lg px-3 py-2 text-sm bg-app-panel text-app-text focus:outline-none focus:ring-2 focus:ring-primary"
          >
            {EVENT_TYPE_OPTIONS.map((t) => (
              <option key={t} value={t}>{t || 'All event types'}</option>
            ))}
          </select>
        </div>
      )}

      <div className="bg-app-panel rounded-2xl border border-app-border shadow-card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-app-border bg-app-panel-soft">
                <th className="px-4 py-3 text-left font-semibold text-app-muted">Event</th>
                <th className="px-4 py-3 text-left font-semibold text-app-muted">Description</th>
                <th className="px-4 py-3 text-left font-semibold text-app-muted">Target</th>
                <th className="px-4 py-3 text-left font-semibold text-app-muted">Time</th>
              </tr>
            </thead>
            <tbody>
              {data.content.length === 0 && (
                <tr>
                  <td colSpan={4} className="px-4 py-8 text-center text-app-muted">
                    No audit events found.
                  </td>
                </tr>
              )}
              {data.content.map((event) => (
                <tr key={event.id} className="border-b border-app-border last:border-0 hover:bg-app-panel-soft transition-colors">
                  <td className="px-4 py-3 align-middle">{eventTypeBadge(event.eventType)}</td>
                  <td className="px-4 py-3 align-middle text-app-text max-w-xs truncate">
                    {event.description ?? '—'}
                  </td>
                  <td className="px-4 py-3 align-middle text-app-muted">
                    {event.targetEntity
                      ? `${event.targetEntity}${event.targetId ? ' #' + event.targetId : ''}`
                      : '—'}
                  </td>
                  <td className="px-4 py-3 align-middle text-app-muted whitespace-nowrap">
                    {new Date(event.createdAt).toLocaleString()}
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
