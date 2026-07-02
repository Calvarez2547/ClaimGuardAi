import { Bot, Eye, FileText } from 'lucide-react';
import { Link } from 'react-router-dom';
import { Badge } from './Badge';
import { formatCurrency, formatDate, labelize, riskTone, statusTone } from '../utils/format';
import type { ClaimSummary, RecentClaim, RiskCategory } from '../types/api';

type ClaimTableRow = ClaimSummary & {
  latestRiskScore?: number | null;
  latestRiskCategory?: RiskCategory | null;
  humanReviewRequired?: boolean | null;
};

function isRecentClaim(row: ClaimSummary | RecentClaim): row is RecentClaim {
  return 'claimId' in row;
}

function normalizeClaim(row: ClaimSummary | RecentClaim): ClaimTableRow {
  if (isRecentClaim(row)) {
    return {
      id: row.claimId,
      claimNumber: row.claimNumber,
      patientControlNumber: row.patientControlNumber,
      payerName: 'Dashboard summary',
      providerName: 'Provider unavailable',
      serviceDate: row.createdAt,
      billedAmount: row.billedAmount,
      claimStatus: row.status,
      createdAt: row.createdAt,
      updatedAt: row.updatedAt,
      latestRiskScore: row.latestRiskScore,
      latestRiskCategory: row.latestRiskCategory,
      humanReviewRequired: row.humanReviewRequired,
    };
  }

  return row;
}

export function ClaimTable({ claims, compact = false }: { claims: (ClaimSummary | RecentClaim)[]; compact?: boolean }) {
  return (
    <div className="w-full overflow-x-auto">
      <table className="w-full min-w-[860px] border-collapse text-sm">
        <thead>
          <tr>
            {['Claim ID', 'Patient Account', ...(!compact ? ['Provider'] : []), 'Service Date', 'Billed Amount', 'Risk', 'Status', 'Actions'].map((col) => (
              <th key={col} className="px-3 py-[14px] border-b border-app-border text-left align-middle text-[#41536c] bg-app-panel-soft text-xs uppercase tracking-[0.03em]">
                {col}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {claims.map((rawClaim) => {
            const claim = normalizeClaim(rawClaim);
            return (
              <tr key={claim.id}>
                <td className="px-3 py-[14px] border-b border-app-border align-middle">
                  <Link to={`/claims/${claim.id}`} className="text-primary font-bold">
                    {claim.claimNumber}
                  </Link>
                </td>
                <td className="px-3 py-[14px] border-b border-app-border align-middle">{claim.patientControlNumber || 'Not provided'}</td>
                {!compact ? <td className="px-3 py-[14px] border-b border-app-border align-middle">{claim.providerName}</td> : null}
                <td className="px-3 py-[14px] border-b border-app-border align-middle">{formatDate(claim.serviceDate)}</td>
                <td className="px-3 py-[14px] border-b border-app-border align-middle">{formatCurrency(claim.billedAmount)}</td>
                <td className="px-3 py-[14px] border-b border-app-border align-middle">
                  {claim.latestRiskCategory ? (
                    <div className="flex items-center gap-2">
                      {claim.latestRiskScore ?? '-'}
                      <Badge tone={riskTone(claim.latestRiskCategory)}>{labelize(claim.latestRiskCategory)}</Badge>
                    </div>
                  ) : (
                    <Badge>Not analyzed</Badge>
                  )}
                </td>
                <td className="px-3 py-[14px] border-b border-app-border align-middle">
                  <Badge tone={statusTone(claim.claimStatus)}>{labelize(claim.claimStatus)}</Badge>
                </td>
                <td className="px-3 py-[14px] border-b border-app-border align-middle">
                  <div className="flex items-center gap-2 flex-wrap">
                    <Link to={`/claims/${claim.id}`} className="inline-flex items-center gap-1 text-primary font-bold whitespace-nowrap" title="View claim">
                      <Eye size={16} /> View
                    </Link>
                    <Link to={`/claims/${claim.id}?analyze=true`} className="inline-flex items-center gap-1 text-primary font-bold whitespace-nowrap" title="Analyze claim">
                      <Bot size={16} /> Analyze
                    </Link>
                    {!compact ? (
                      <Link to={`/claims/${claim.id}#notes`} className="inline-flex items-center gap-1 text-primary font-bold whitespace-nowrap" title="Add note">
                        <FileText size={16} /> Add Note
                      </Link>
                    ) : null}
                  </div>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
