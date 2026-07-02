export type UserRole =
  | 'BILLING_SPECIALIST'
  | 'REVENUE_CYCLE_ANALYST'
  | 'CODING_REVIEWER'
  | 'REVENUE_CYCLE_MANAGER'
  | 'ADMINISTRATOR';

export type ClaimStatus =
  | 'RECEIVED'
  | 'DRAFT'
  | 'SUBMITTED'
  | 'IN_REVIEW'
  | 'NEEDS_INFO'
  | 'APPROVED'
  | 'DENIED'
  | 'CLOSED';

export type RiskCategory = 'LOW' | 'MEDIUM' | 'HIGH';

export type CurrentUser = {
  id: number;
  username: string;
  email: string;
  roles: UserRole[];
};

export type LoginResponse = {
  accessToken: string;
  tokenType: string;
  expiresAt: string;
  refreshToken: string;
};

export type ApiErrorDetail = {
  field?: string;
  message: string;
};

export type ApiErrorResponse = {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  path?: string;
  correlationId?: string;
  details?: ApiErrorDetail[];
};

export type ClaimSummary = {
  id: number;
  claimNumber: string;
  patientControlNumber?: string | null;
  payerName: string;
  providerName: string;
  serviceDate: string;
  billedAmount: number;
  claimStatus: ClaimStatus;
  createdAt: string;
  updatedAt: string;
};

export type Claim = ClaimSummary & {
  priorAuthRequired: boolean;
  priorAuthNumber?: string | null;
  claimNotes?: string | null;
  createdByUserId: number;
};

export type CreateClaimPayload = {
  claimNumber: string;
  patientControlNumber?: string | null;
  payerName: string;
  providerName: string;
  serviceDate: string;
  billedAmount: number;
  priorAuthRequired?: boolean;
  priorAuthNumber?: string | null;
  claimNotes?: string | null;
};

export type ReviewNote = {
  id: number;
  claimId: number;
  noteText: string;
  createdAt: string;
  updatedAt: string;
};

export type AnalysisFinding = {
  findingId: number;
  findingCode: string;
  description: string;
  points: number;
  category: string;
  label: string;
  severity: string;
  weight: number;
  triggered: boolean;
  contribution: number;
  recommendedAction: string;
};

export type RiskFactor = {
  code: string;
  category: string;
  label: string;
  description: string;
  severity: string;
  weight: number;
  triggered: boolean;
  contribution: number;
  recommendedAction: string;
};

export type RiskScoreBreakdown = {
  baseScore: number;
  totalScore: number;
  cappedScore: number;
  riskCategory: RiskCategory;
  primaryRiskReason: string;
  secondaryRiskReasons: string[];
  humanReviewRequired: boolean;
  factors: RiskFactor[];
  recommendedActions: string[];
};

export type ClaimAnalysis = {
  analysisId: number;
  claimId: number;
  riskScore: number;
  riskCategory: RiskCategory;
  primaryRiskReason: string;
  secondaryRiskReasons: string[];
  findings: AnalysisFinding[];
  scoreBreakdown: RiskScoreBreakdown;
  aiSummary?: string | null;
  recommendedActions: string[];
  humanReviewRequired: boolean;
  fallbackUsed: boolean;
  createdAt: string;
};

export type ClaimStatusSummary = {
  status: ClaimStatus;
  count: number;
};

export type RiskCategorySummary = {
  riskCategory: RiskCategory;
  count: number;
};

export type RecentClaim = {
  claimId: number;
  claimNumber: string;
  patientControlNumber?: string | null;
  status: ClaimStatus;
  billedAmount: number;
  createdAt: string;
  updatedAt: string;
  latestRiskScore?: number | null;
  latestRiskCategory?: RiskCategory | null;
  humanReviewRequired?: boolean | null;
};

export type RecentAnalysis = {
  analysisId: number;
  claimId: number;
  claimNumber: string;
  patientControlNumber?: string | null;
  status: ClaimStatus;
  riskScore: number;
  riskCategory: RiskCategory;
  primaryRiskReason: string;
  humanReviewRequired: boolean;
  analyzedAt: string;
};

export type HighRiskClaim = {
  claimId: number;
  claimNumber: string;
  patientControlNumber?: string | null;
  status: ClaimStatus;
  billedAmount: number;
  latestRiskScore: number;
  latestRiskCategory: RiskCategory;
  primaryRiskReason: string;
  humanReviewRequired: boolean;
};

export type CommonRiskFactor = {
  code: string;
  category: string;
  label: string;
  count: number;
  totalContribution: number;
};

export type AdminUser = {
  id: number;
  username: string;
  email: string;
  enabled: boolean;
  roles: string[];
  createdAt: string;
  updatedAt: string;
};

export type PageResponse<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
};

export type AuditEventItem = {
  id: number;
  eventType: string;
  actorUserId: number | null;
  targetEntity: string | null;
  targetId: string | null;
  ipAddress: string | null;
  correlationId: string | null;
  description: string | null;
  createdAt: string;
};

export type DashboardSummary = {
  totalClaims: number;
  claimsByStatus: ClaimStatusSummary[];
  analysesByRiskCategory: RiskCategorySummary[];
  humanReviewRequiredCount: number;
  lowRiskCount: number;
  mediumRiskCount: number;
  highRiskCount: number;
  recentClaims: RecentClaim[];
  recentAnalyses: RecentAnalysis[];
  highestRiskClaims: HighRiskClaim[];
  topRiskFactors: CommonRiskFactor[];
  generatedAt: string;
};
