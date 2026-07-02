const TOKEN_KEY = 'claimguardai.accessToken';
const EXPIRES_AT_KEY = 'claimguardai.expiresAt';
const REFRESH_TOKEN_KEY = 'claimguardai.refreshToken';
const LAST_USERNAME_KEY = 'claimguardai.lastUsername';

export function getStoredToken() {
  return window.localStorage.getItem(TOKEN_KEY);
}

export function setStoredToken(token: string, expiresAt?: string, refreshToken?: string) {
  window.localStorage.setItem(TOKEN_KEY, token);

  if (expiresAt) {
    window.localStorage.setItem(EXPIRES_AT_KEY, expiresAt);
  }

  if (refreshToken) {
    window.localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
  }
}

export function clearStoredToken() {
  window.localStorage.removeItem(TOKEN_KEY);
  window.localStorage.removeItem(EXPIRES_AT_KEY);
  window.localStorage.removeItem(REFRESH_TOKEN_KEY);
}

export function getStoredTokenExpiration() {
  return window.localStorage.getItem(EXPIRES_AT_KEY);
}

export function getStoredRefreshToken() {
  return window.localStorage.getItem(REFRESH_TOKEN_KEY);
}

export function setLastUsername(username: string) {
  window.localStorage.setItem(LAST_USERNAME_KEY, username);
}

export function getLastUsername() {
  return window.localStorage.getItem(LAST_USERNAME_KEY);
}

export function clearLastUsername() {
  window.localStorage.removeItem(LAST_USERNAME_KEY);
}
