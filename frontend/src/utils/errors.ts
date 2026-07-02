import { ApiError } from '../api/http';
import { env } from '../config/env';

export function friendlyError(error: unknown) {
  if (error instanceof ApiError) {
    const details = error.details.details || [];

    if (details.length > 0) {
      return details.map((detail) => `${detail.field ? `${detail.field}: ` : ''}${detail.message}`).join(' ');
    }

    return error.details.message || error.message;
  }

  if (error instanceof Error) {
    if (error.message === 'Failed to fetch') {
      return `Cannot reach the ClaimGuard AI backend at ${env.apiBaseUrl}. Start the backend with the local profile, then try signing in again.`;
    }

    return error.message;
  }

  return 'Something went wrong. Please try again.';
}
