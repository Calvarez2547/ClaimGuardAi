import { AlertTriangle, Bot, ClipboardList, Clock } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useEffect, useMemo, useState } from 'react';
import { claimGuardApi } from '../api/claimGuardApi';
import { ClaimTable } from '../components/ClaimTable';
import { EmptyState, ErrorState, LoadingState } from '../components/State';
import { MetricCard } from '../components/MetricCard';
import { RingChart } from '../components/RingChart';
import { Badge } from '../components/Badge';
import type { DashboardSummary } from '../types/api';
import { formatDateTime, labelize, riskTone } from '../utils/format';
import { friendlyError } from '../utils/errors';

export function DashboardPage() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    claimGuardApi
      .getDashboardSummary()
      .then(setSummary)
      .catch((e) => setError(friendlyError(e)))
      .finally(() => setIsLoading(false));
  }, []);

  const pendingReview = useMemo(
    () =>
      summary?.claimsByStatus
        .filter((item) => ['RECEIVED', 'IN_REVIEW', 'NEEDS_INFO'].includes(item.status))
        .reduce((total, item) => total + item.count, 0) || 0,
    [summary],
  );

  if (isLoading) return <LoadingState label="Loading dashboard summary..." />;
  if (error) return <ErrorState message={error} />;
  if (!summary) return <EmptyState title="No dashboard data yet" description="Create a claim to populate the dashboard." />;

  const statusTotal = summary.claimsByStatus.reduce((total, item) => total + item.count, 0);
  const riskTotal = summary.lowRiskCount + summary.mediumRiskCount + summary.highRiskCount;

  return (
    <div className="grid gap-[22px]">
      {/* Metric cards */}
      <div className="grid grid-cols-4 max-lg2:grid-cols-2 max-md2:grid-cols-1 gap-[18px]">
        <MetricCard label="Total Claims" value={summary.totalClaims} delta="Owner-scoped claims" icon={ClipboardList} />
        <MetricCard label="High Risk Claims" value={summary.highRiskCount} delta="Latest persisted analyses" icon={AlertTriangle} tone="red" />
        <MetricCard label="Pending Review" value={pendingReview} delta="Received, in review, needs info" icon={Clock} tone="orange" />
        <MetricCard label="AI Analyses" value={summary.recentAnalyses.length} delta="Recent persisted records" icon={Bot} tone="teal" />
      </div>

      {/* Ring charts */}
      <div className="grid grid-cols-2 max-md2:grid-cols-1 gap-[22px]">
        <section className="p-5 border border-app-border rounded bg-app-panel shadow-card">
          <div className="flex items-center justify-between gap-4">
            <h2 className="m-0 flex items-center gap-2 text-[18px]">Claims by Status</h2>
            <Link to="/claims" className="text-primary font-bold">View all claims</Link>
          </div>
          <RingChart
            total={statusTotal}
            segments={summary.claimsByStatus.map((item, index) => ({
              label: labelize(item.status),
              value: item.count,
              color: ['#2f80ed', '#35c59f', '#ffbe3d', '#ff6b6b', '#8fa3b8', '#1c9be8', '#7c8a9a', '#142f52'][index % 8],
            }))}
          />
        </section>

        <section className="p-5 border border-app-border rounded bg-app-panel shadow-card">
          <div className="flex items-center justify-between gap-4">
            <h2 className="m-0 flex items-center gap-2 text-[18px]">Risk Distribution</h2>
            <Link to="/analysis-history" className="text-primary font-bold">View analyses</Link>
          </div>
          <RingChart
            total={riskTotal}
            segments={[
              { label: 'High Risk', value: summary.highRiskCount, color: '#ef4444' },
              { label: 'Moderate Risk', value: summary.mediumRiskCount, color: '#f59e0b' },
              { label: 'Low Risk', value: summary.lowRiskCount, color: '#10b981' },
            ]}
          />
        </section>
      </div>

      {/* Recent claims / analyses */}
      <div className="grid grid-cols-2 max-md2:grid-cols-1 gap-[22px]">
        <section className="p-5 border border-app-border rounded bg-app-panel shadow-card">
          <div className="flex items-center justify-between gap-4 mb-2">
            <h2 className="m-0 text-[18px]">Recent Claims</h2>
            <Link to="/claims" className="text-primary font-bold">View all</Link>
          </div>
          {summary.recentClaims.length > 0 ? (
            <ClaimTable claims={summary.recentClaims.slice(0, 5)} compact />
          ) : (
            <EmptyState title="No claims yet" description="Create the first demo claim to start the workflow." />
          )}
        </section>

        <section className="p-5 border border-app-border rounded bg-app-panel shadow-card">
          <div className="flex items-center justify-between gap-4 mb-2">
            <h2 className="m-0 text-[18px]">Recent Analyses</h2>
            <Link to="/analysis-history" className="text-primary font-bold">View all</Link>
          </div>
          {summary.recentAnalyses.length > 0 ? (
            <div className="grid gap-[14px]">
              {summary.recentAnalyses.slice(0, 6).map((analysis) => (
                <Link
                  key={analysis.analysisId}
                  to={`/claims/${analysis.claimId}`}
                  className="grid grid-cols-[minmax(0,1fr)_auto_auto] items-center gap-[14px] p-3 border border-app-border rounded"
                >
                  <div>
                    <strong>{analysis.claimNumber}</strong>
                    <p className="overflow-hidden m-[3px_0_0] text-app-muted text-ellipsis whitespace-nowrap">{analysis.primaryRiskReason}</p>
                  </div>
                  <Badge tone={riskTone(analysis.riskCategory)}>{labelize(analysis.riskCategory)}</Badge>
                  <span className="text-app-muted text-[13px]">{formatDateTime(analysis.analyzedAt)}</span>
                </Link>
              ))}
            </div>
          ) : (
            <EmptyState title="No analyses yet" description="Run an analysis from a claim detail page." />
          )}
        </section>
      </div>

      {/* Top risk factors */}
      {summary.topRiskFactors.length > 0 ? (
        <section className="p-5 border border-app-border rounded bg-app-panel shadow-card">
          <div className="flex items-center justify-between gap-4">
            <h2 className="m-0 text-[18px]">Top Risk Factors</h2>
            <span className="text-app-muted text-sm">Generated {formatDateTime(summary.generatedAt)}</span>
          </div>
          <div className="grid grid-cols-4 max-lg2:grid-cols-2 max-md2:grid-cols-1 gap-3 mt-4">
            {summary.topRiskFactors.map((factor) => (
              <div key={factor.code} className="grid gap-[7px] p-[14px] border border-app-border rounded">
                <Badge tone="info">{labelize(factor.category)}</Badge>
                <strong>{factor.label}</strong>
                <span className="text-app-muted text-sm">{factor.count} hits, {factor.totalContribution} points</span>
              </div>
            ))}
          </div>
        </section>
      ) : null}

      <div className="text-app-muted text-[13px] text-center">
        Secure prototype | Backend-owned scoring | No real PHI
      </div>
    </div>
  );
}
