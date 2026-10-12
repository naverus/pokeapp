import type { Role } from './types';

export interface Session {
  token: string;
  username: string;
  role: Role;
  expiresAt: string;
}

const STORAGE_KEY = 'pokeapp.session';
export const SESSION_EXPIRED_EVENT = 'pokeapp:session-expired';

export const tokenStorage = {
  load(): Session | null {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return null;
    try {
      const session = JSON.parse(raw) as Session;
      if (new Date(session.expiresAt).getTime() <= Date.now()) {
        localStorage.removeItem(STORAGE_KEY);
        return null;
      }
      return session;
    } catch {
      localStorage.removeItem(STORAGE_KEY);
      return null;
    }
  },
  save(session: Session): void {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
  },
  clear(): void {
    localStorage.removeItem(STORAGE_KEY);
  },
};
