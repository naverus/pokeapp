import { render } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient } from '@tanstack/react-query';
import { MemoryRouter } from 'react-router-dom';
import { App } from '../App';
import { AppProviders } from '../AppProviders';
import { tokenStorage } from '../shared/api/tokenStorage';
import type { Role } from '../shared/api/types';

interface RenderOptions {
  as?: { username: string; role: Role };
}

export function renderApp(route: string, { as }: RenderOptions = {}) {
  if (as) {
    tokenStorage.save({
      token: 'jwt-token',
      username: as.username,
      role: as.role,
      expiresAt: new Date(Date.now() + 3_600_000).toISOString(),
    });
  }
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  const user = userEvent.setup();
  const view = render(
    <AppProviders client={client}>
      <MemoryRouter initialEntries={[route]}>
        <App />
      </MemoryRouter>
    </AppProviders>,
  );
  return { user, ...view };
}

export const ASH = { username: 'ash', role: 'USER' as const };
export const ADMIN = { username: 'admin', role: 'ADMIN' as const };
