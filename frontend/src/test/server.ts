import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { page, pikachuDetail, pikachuSummary, storedPikachu } from './fixtures';

export const API = '*/api/v1';

export const handlers = [
  http.get(`${API}/pokemon`, () => HttpResponse.json(page([pikachuSummary], 0, 12, 1))),
  http.get(`${API}/pokemon/:idOrName`, () => HttpResponse.json(pikachuDetail)),
  http.get(`${API}/local-pokemon`, () => HttpResponse.json(page([storedPikachu]))),
  http.get(`${API}/admin/sync-jobs/latest`, () => new HttpResponse(null, { status: 204 })),
  http.post(`${API}/auth/login`, async ({ request }) => {
    const body = (await request.json()) as { username: string; password: string };
    if (body.password !== 'pikachu123' && body.password !== 'admin123') {
      return HttpResponse.json(
        { status: 401, title: 'Authentication failed', detail: 'Invalid username or password' },
        { status: 401 },
      );
    }
    return HttpResponse.json({
      accessToken: 'jwt-token',
      tokenType: 'Bearer',
      expiresAt: new Date(Date.now() + 3_600_000).toISOString(),
      username: body.username,
      role: body.username === 'admin' ? 'ADMIN' : 'USER',
    });
  }),
];

export const server = setupServer(...handlers);
