import { createContext } from 'react';
import type { Session } from '../../shared/api/tokenStorage';
import type { Credentials } from './api';

export interface AuthContextValue {
  session: Session | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  login: (credentials: Credentials) => Promise<void>;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextValue | null>(null);
