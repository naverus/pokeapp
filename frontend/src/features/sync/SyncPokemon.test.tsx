import { screen, within } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import { describe, expect, it } from 'vitest';
import type { SyncJob, SyncRangePayload } from '../../shared/api/types';
import { page, storedPikachu } from '../../test/fixtures';
import { ADMIN, ASH, renderApp } from '../../test/renderApp';
import { API, server } from '../../test/server';

const runningJob: SyncJob = {
  id: 'job-1',
  status: 'RUNNING',
  fromId: 1,
  toId: 3,
  total: 3,
  processed: 0,
  inserted: 0,
  updated: 0,
  failed: 0,
  errors: [],
  requestedBy: 'admin',
  startedAt: '2026-01-01T00:00:00Z',
  finishedAt: null,
};

const completedJob: SyncJob = {
  ...runningJob,
  status: 'COMPLETED',
  processed: 3,
  inserted: 1,
  updated: 1,
  failed: 1,
  errors: ['#3: not found in PokeAPI'],
  finishedAt: '2026-01-01T00:00:05Z',
};

describe('Sync Pokemon data (US03, admin)', () => {
  it('is hidden for regular users', async () => {
    renderApp('/collection', { as: ASH });

    await screen.findByRole('heading', { name: 'Pikachu MX' });
    expect(screen.queryByRole('button', { name: 'Sync Pokemon data' })).not.toBeInTheDocument();
  });

  it('starts a sync, shows progress and refreshes the collection when it finishes', async () => {
    let requested: SyncRangePayload | null = null;
    let polls = 0;
    let collectionLoads = 0;
    server.use(
      http.get(`${API}/local-pokemon`, () => {
        collectionLoads += 1;
        return HttpResponse.json(page([storedPikachu]));
      }),
      http.post(`${API}/admin/sync-jobs`, async ({ request }) => {
        requested = (await request.json()) as SyncRangePayload;
        return HttpResponse.json(runningJob, { status: 202 });
      }),
      http.get(`${API}/admin/sync-jobs/:id`, () => {
        polls += 1;
        return HttpResponse.json(
          polls === 1 ? { ...runningJob, processed: 1, inserted: 1 } : completedJob,
        );
      }),
    );
    const { user } = renderApp('/collection', { as: ADMIN });

    await user.click(await screen.findByRole('button', { name: 'Sync Pokemon data' }));
    const dialog = await screen.findByRole('dialog', { name: 'Sync Pokémon data' });
    const to = within(dialog).getByLabelText('To #');
    await user.clear(to);
    await user.type(to, '3');
    await user.click(within(dialog).getByRole('button', { name: 'Start sync' }));

    expect(await within(dialog).findByLabelText('Sync progress')).toBeInTheDocument();
    expect(within(dialog).getByRole('button', { name: 'Start sync' })).toBeDisabled();
    expect(requested).toEqual({ fromId: 1, toId: 3 });

    expect(
      await screen.findByText('Synced: 1 new, 1 updated, 1 failed', {}, { timeout: 4000 }),
    ).toBeInTheDocument();
    expect(within(dialog).getByText('#3: not found in PokeAPI')).toBeInTheDocument();
    expect(collectionLoads).toBeGreaterThan(1);
  });

  it('validates the range on the client', async () => {
    const { user } = renderApp('/collection', { as: ADMIN });

    await user.click(await screen.findByRole('button', { name: 'Sync Pokemon data' }));
    const dialog = await screen.findByRole('dialog', { name: 'Sync Pokémon data' });
    const from = within(dialog).getByLabelText('From #');
    await user.clear(from);
    await user.type(from, '200');
    await user.click(within(dialog).getByRole('button', { name: 'Start sync' }));

    expect(
      await within(dialog).findByText('Must be greater than or equal to From'),
    ).toBeInTheDocument();
  });

  it('explains that another sync is already running', async () => {
    server.use(
      http.post(`${API}/admin/sync-jobs`, () =>
        HttpResponse.json(
          {
            status: 409,
            title: 'Conflict',
            detail: 'A Pokemon synchronization is already running',
          },
          { status: 409 },
        ),
      ),
    );
    const { user } = renderApp('/collection', { as: ADMIN });

    await user.click(await screen.findByRole('button', { name: 'Sync Pokemon data' }));
    const dialog = await screen.findByRole('dialog', { name: 'Sync Pokémon data' });
    await user.click(within(dialog).getByRole('button', { name: 'Start sync' }));

    expect(
      await within(dialog).findByText('A Pokemon synchronization is already running'),
    ).toBeInTheDocument();
  });

  it('shows the result of the latest sync when reopened', async () => {
    server.use(
      http.get(`${API}/admin/sync-jobs/latest`, () => HttpResponse.json(completedJob)),
      http.get(`${API}/admin/sync-jobs/:id`, () => HttpResponse.json(completedJob)),
    );
    const { user } = renderApp('/collection', { as: ADMIN });

    await user.click(await screen.findByRole('button', { name: 'Sync Pokemon data' }));
    const dialog = await screen.findByRole('dialog', { name: 'Sync Pokémon data' });

    expect(
      await within(dialog).findByText(/Last sync \(#1–#3\) finished: 1 new, 1 updated, 1 failed/),
    ).toBeInTheDocument();
  });
});
