import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { claimGuardApi } from '../api/claimGuardApi';
import { Badge } from '../components/Badge';
import { EmptyState, ErrorState, LoadingState } from '../components/State';
import type { DashboardSummary } from '../types/api';
import { formatDateTime, labelize, riskTone } from '../utils/format';
import { friendlyError } from '../utils/errors';

export function AnalysisHistoryPage() {
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

  if (isLoading) return <LoadingState label="Loading analysis history..." />;
  if (error) return <ErrorState message={error} />;

  const analyses = summary?.recentAnalyses || [];

  return (
    <section className="p-5 border border-app-border rounded bg-app-panel shadow-card">
      <div className="flex items-center justify-between gap-4 mb-4">
        <h2 className="m-0 text-[18px]">Recent Analyses</h2>
        <span className="text-app-muted text-sm">Dashboard-backed recent persisted analyses</span>
      </div>
      {analyses.length > 0 ? (
        <div className="grid gap-[14px]">
          {analyses.map((analysis) => (
            <Link
              key={analysis.analysisId}
              to={`/claims/${analysis.claimId}`}
              className="grid grid-cols-[minmax(0,1fr)_auto] items-center gap-[14px] p-3 border border-app-border rounded"
            >
              <div>
                <strong>{analysis.claimNumber}</strong>
                <p className="overflow-hidden m-[3px_0_0] text-app-muted text-ellipsis whitespace-nowrap">{analysis.primaryRiskReason}</p>
                <span className="block text-app-muted text-[13px]">{formatDateTime(analysis.analyzedAt)}</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="grid w-[42px] h-[42px] place-items-center rounded-full bg-navy text-white font-extrabold">
                  {analysis.riskScore}
                </span>
                <Badge tone={riskTone(analysis.riskCategory)}>{labelize(analysis.riskCategory)}</Badge>
                {analysis.humanReviewRequired
                  ? <Badge tone="danger">Human review</Badge>
                  : <Badge tone="success">Routine</Badge>
                }
              </div>
            </Link>
          ))}
        </div>
      ) : (
        <EmptyState title="No recent analyses" description="Open a claim and run analysis to create history records." />
      )}
    </section>
  );
}
