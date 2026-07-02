import type { ClaimStatus, RiskCategory } from '../types/api';

export function formatCurrency(value?: number | null) {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    maximumFractionDigits: 2,
  }).format(Number(value || 0));
}

export function formatDate(value?: string | null) {
  if (!value) {
    return 'Not available';
  }

  return new Intl.DateTimeFormat('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  }).format(new Date(value));
}

export function formatDateTime(value?: string | null) {
  if (!value) {
    return 'Not available';
  }

  return new Intl.DateTimeFormat('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  }).format(new Date(value));
}

export function labelize(value?: string | null) {
  if (!value) {
    return 'Unknown';
  }

  return value
    .toLowerCase()
    .split('_')
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
}

export function statusTone(status?: ClaimStatus) {
  switch (status) {
    case 'APPROVED':
    case 'CLOSED':
      return 'success';
    case 'DENIED':
    case 'NEEDS_INFO':
      return 'danger';
    case 'IN_REVIEW':
    case 'SUBMITTED':
      return 'info';
    case 'DRAFT':
      return 'neutral';
    case 'RECEIVED':
    default:
      return 'warning';
  }
}

export function riskTone(risk?: RiskCategory | null) {
  switch (risk) {
    case 'HIGH':
      return 'danger';
    case 'MEDIUM':
      return 'warning';
    case 'LOW':
      return 'success';
    default:
      return 'neutral';
  }
}

export function initials(name?: string | null) {
  if (!name) {
    return 'CG';
  }

  return name
    .split(/[.\s@_-]+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join('');
}
