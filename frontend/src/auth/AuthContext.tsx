/* eslint-disable react-refresh/only-export-components */
import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { claimGuardApi } from '../api/claimGuardApi';
import {
  clearStoredToken,
  getStoredRefreshToken,
  getStoredToken,
  setStoredToken,
  setLastUsername,
} from './tokenStorage';
import type { CurrentUser, UserRole } from '../types/api';

type AuthContextValue = {
  user: CurrentUser | null;
  isAuthenticated: boolean;
  isBootstrapping: boolean;
  hasRole: (role: UserRole) => boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  refreshUser: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<CurrentUser | null>(null);
  const [isBootstrapping, setIsBootstrapping] = useState(true);

  const logout = useCallback(() => {
    const refreshToken = getStoredRefreshToken();
    clearStoredToken();
    setUser(null);

    if (refreshToken) {
      claimGuardApi.logout(refreshToken).catch(() => {});
    }
  }, []);

  const refreshUser = useCallback(async () => {
    const token = getStoredToken();

    if (!token) {
      setUser(null);
      return;
    }

    const currentUser = await claimGuardApi.me();
    setUser(currentUser);
  }, []);

  const login = useCallback(async (username: string, password: string) => {
    const response = await claimGuardApi.login(username, password);
    setStoredToken(response.accessToken, response.expiresAt, response.refreshToken);
    setLastUsername(username);
    const currentUser = await claimGuardApi.me();
    setUser(currentUser);
  }, []);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    refreshUser()
      .catch(() => logout())
      .finally(() => setIsBootstrapping(false));
  }, [logout, refreshUser]);

  useEffect(() => {
    window.addEventListener('claimguardai:unauthorized', logout);
    return () => window.removeEventListener('claimguardai:unauthorized', logout);
  }, [logout]);

  const hasRole = useCallback(
    (role: UserRole) => Boolean(user?.roles?.includes(role)),
    [user],
  );

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isAuthenticated: Boolean(user),
      isBootstrapping,
      hasRole,
      login,
      logout,
      refreshUser,
    }),
    [isBootstrapping, hasRole, login, logout, refreshUser, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const value = useContext(AuthContext);

  if (!value) {
    throw new Error('useAuth must be used inside AuthProvider.');
  }

  return value;
}
