import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { SESSION_EXPIRED_EVENT, tokenStorage, type Session } from '../../shared/api/tokenStorage';
import { queryKeys } from '../../shared/api/queryKeys';
import { login as loginRequest, type Credentials } from './api';
import { AuthContext, type AuthContextValue } from './authContext';

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient();
  const [session, setSession] = useState<Session | null>(() => tokenStorage.load());

  const logout = useCallback(() => {
    tokenStorage.clear();
    setSession(null);
    queryClient.removeQueries({ queryKey: queryKeys.localAll });
  }, [queryClient]);

  useEffect(() => {
    const onExpired = () => setSession(null);
    window.addEventListener(SESSION_EXPIRED_EVENT, onExpired);
    return () => window.removeEventListener(SESSION_EXPIRED_EVENT, onExpired);
  }, []);

  const login = useCallback(async (credentials: Credentials) => {
    const token = await loginRequest(credentials);
    const next: Session = {
      token: token.accessToken,
      username: token.username,
      role: token.role,
      expiresAt: token.expiresAt,
    };
    tokenStorage.save(next);
    setSession(next);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      session,
      isAuthenticated: session !== null,
      isAdmin: session?.role === 'ADMIN',
      login,
      logout,
    }),
    [session, login, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
