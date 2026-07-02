import { Plus, Search } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useEffect, useMemo, useState } from 'react';
import { claimGuardApi } from '../api/claimGuardApi';
import { Badge } from '../components/Badge';
import { ClaimTable } from '../components/ClaimTable';
import { EmptyState, ErrorState, LoadingState } from '../components/State';
import type { ClaimStatus, ClaimSummary } from '../types/api';
import { formatCurrency, labelize, statusTone } from '../utils/format';
import { friendlyError } from '../utils/errors';

const statusFilters: ('ALL' | ClaimStatus)[] = [
  'ALL', 'RECEIVED', 'IN_REVIEW', 'NEEDS_INFO', 'SUBMITTED', 'APPROVED', 'DENIED', 'CLOSED',
];

const summaryCards = [
  { key: 'blue', label: 'Open', desc: 'Received or draft', statuses: ['RECEIVED', 'DRAFT'] as ClaimStatus[], gradient: 'from-white to-primary-soft' },
  { key: 'orange', label: 'Needs Review', desc: 'Review workflow', statuses: ['IN_REVIEW', 'NEEDS_INFO'] as ClaimStatus[], gradient: 'from-white to-c-orange-soft' },
  { key: 'red', label: 'Denied Risk', desc: 'Denied claims', statuses: ['DENIED'] as ClaimStatus[], gradient: 'from-white to-c-red-soft' },
  { key: 'green', label: 'Ready to Submit', desc: 'Approved or submitted', statuses: ['APPROVED', 'SUBMITTED'] as ClaimStatus[], gradient: 'from-white to-c-green-soft' },
];

export function ClaimsListPage() {
  const [claims, setClaims] = useState<ClaimSummary[]>([]);
  const [error, setError] = useState('');
  const [query, setQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<'ALL' | ClaimStatus>('ALL');
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    claimGuardApi
      .listClaims()
      .then(setClaims)
      .catch((e) => setError(friendlyError(e)))
      .finally(() => setIsLoading(false));
  }, []);

  const filteredClaims = useMemo(() => {
    const q = query.trim().toLowerCase();
    return claims.filter((claim) => {
      const matchesStatus = statusFilter === 'ALL' || claim.claimStatus === statusFilter;
      const matchesQuery =
        !q ||
        [claim.claimNumber, claim.patientControlNumber, claim.payerName, claim.providerName]
          .filter(Boolean)
          .some((v) => String(v).toLowerCase().includes(q));
      return matchesStatus && matchesQuery;
    });
  }, [claims, query, statusFilter]);

  const totalValue = claims.reduce((sum, c) => sum + Number(c.billedAmount || 0), 0);

  if (isLoading) return <LoadingState label="Loading claims..." />;
  if (error) return <ErrorState message={error} />;

  return (
    <div className="grid gap-[22px]">
      {/* Header row */}
      <div className="flex items-center justify-between gap-4">
        <div>
          <h2 className="m-0 text-app-text text-2xl leading-tight">Claims workspace</h2>
          <p className="mt-1 mb-0 text-app-muted text-sm">{claims.length} total claims | {formatCurrency(totalValue)} billed demo value</p>
        </div>
        <Link to="/claims/new" className="inline-flex items-center justify-center gap-2 min-h-[40px] px-[14px] rounded font-extrabold border border-primary text-white bg-primary">
          <Plus size={17} /> New claim
        </Link>
      </div>

      {/* Summary cards */}
      <div className="grid grid-cols-4 max-lg2:grid-cols-2 max-md2:grid-cols-1 gap-[18px]">
        {summaryCards.map(({ key, label, desc, statuses, gradient }) => {
          const count = claims.filter((c) => statuses.includes(c.claimStatus)).length;
          return (
            <div key={key} className={`p-[18px] border border-app-border rounded shadow-card bg-gradient-to-br ${gradient}`}>
              <span className="text-app-muted text-sm">{label}</span>
              <strong className="block mt-1 mb-1 text-[29px]">{count}</strong>
              <p className="m-0 text-app-muted text-sm">{desc}</p>
            </div>
          );
        })}
      </div>

      {/* Claims panel */}
      <section className="p-5 border border-app-border rounded bg-app-panel shadow-card">
        <div className="grid gap-[18px] mb-[18px]">
          {/* Tabs */}
          <div className="flex gap-2 overflow-x-auto">
            {statusFilters.map((status) => (
              <button
                key={status}
                type="button"
                onClick={() => setStatusFilter(status)}
                className={`border-0 border-b-2 pb-[10px] pt-[10px] px-2 bg-transparent font-extrabold whitespace-nowrap ${
                  statusFilter === status
                    ? 'text-primary border-primary'
                    : 'text-app-muted border-transparent'
                }`}
              >
                {status === 'ALL' ? 'All Claims' : labelize(status)}
              </button>
            ))}
          </div>
          {/* Search */}
          <label className="flex items-center gap-2.5 min-w-[260px] max-w-[420px] px-3 border border-app-border rounded text-app-muted bg-white">
            <Search size={17} />
            <input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Search claims..."
              className="w-full h-[42px] border-0 outline-0 text-app-text bg-transparent"
            />
          </label>
        </div>

        {filteredClaims.length > 0 ? (
          <ClaimTable claims={filteredClaims} />
        ) : (
          <EmptyState
            title="No matching claims"
            description="Try a different search or create a new demo claim."
            action={<Link to="/claims/new" className="inline-flex items-center justify-center gap-2 min-h-[40px] px-[14px] rounded font-extrabold border border-primary text-white bg-primary">Create claim</Link>}
          />
        )}
      </section>

      {/* Status mix */}
      <section className="p-5 border border-app-border rounded bg-app-panel shadow-card">
        <h2 className="m-0 mb-3 text-[18px]">Status Mix</h2>
        <div className="grid grid-cols-4 max-lg2:grid-cols-2 max-md2:grid-cols-1 gap-3">
          {statusFilters
            .filter((s) => s !== 'ALL')
            .map((status) => {
              const count = claims.filter((c) => c.claimStatus === status).length;
              return (
                <div key={status} className="flex items-center justify-between p-3 border border-app-border rounded">
                  <Badge tone={statusTone(status)}>{labelize(status)}</Badge>
                  <strong>{count}</strong>
                </div>
              );
            })}
        </div>
      </section>
    </div>
  );
}
