import {
  clearStoredToken,
  getStoredRefreshToken,
  getStoredToken,
  setStoredToken,
} from '../auth/tokenStorage';
import { env } from '../config/env';
import type { ApiErrorResponse, LoginResponse } from '../types/api';

export class ApiError extends Error {
  status: number;
  details: ApiErrorResponse;

  constructor(status: number, details: ApiErrorResponse) {
    super(details.message || details.error || `Request failed with status ${status}`);
    this.name = 'ApiError';
    this.status = status;
    this.details = details;
  }
}

type RequestOptions = Omit<RequestInit, 'body'> & {
  body?: unknown;
  auth?: boolean;
  _retry?: boolean;
};

async function parseResponse(response: Response) {
  const text = await response.text();

  if (!text) {
    return null;
  }

  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

async function tryRefresh(): Promise<boolean> {
  const refreshToken = getStoredRefreshToken();

  if (!refreshToken) {
    return false;
  }

  try {
    const response = await fetch(`${env.apiBaseUrl}/api/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken }),
    });

    if (!response.ok) {
      return false;
    }

    const data = (await response.json()) as LoginResponse;
    setStoredToken(data.accessToken, data.expiresAt, data.refreshToken);
    return true;
  } catch {
    return false;
  }
}

function dispatchUnauthorized() {
  clearStoredToken();
  window.dispatchEvent(new Event('claimguardai:unauthorized'));
}

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const token = getStoredToken();
  const headers = new Headers(options.headers);

  if (!headers.has('Content-Type') && options.body !== undefined) {
    headers.set('Content-Type', 'application/json');
  }

  if (options.auth !== false && token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(`${env.apiBaseUrl}${path}`, {
    ...options,
    headers,
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  });

  const payload = await parseResponse(response);

  if (!response.ok) {
    const details =
      typeof payload === 'object' && payload !== null
        ? (payload as ApiErrorResponse)
        : { status: response.status, message: String(payload || response.statusText) };

    if (response.status === 401 && options.auth !== false && !options._retry) {
      const refreshed = await tryRefresh();

      if (refreshed) {
        return apiRequest<T>(path, { ...options, _retry: true });
      }

      dispatchUnauthorized();
    }

    throw new ApiError(response.status, details);
  }

  return payload as T;
}
