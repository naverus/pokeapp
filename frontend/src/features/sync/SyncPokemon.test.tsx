import { screen, waitFor, within } from '@testing-library/react';
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
  errors: ['#3: no Pokémon with this Pokédex number'],
  finishedAt: '2026-01-01T00:00:05Z',
};

describe('Restock Collection (US03, admin)', () => {
  it('is hidden for regular users', async () => {
    renderApp('/collection', { as: ASH });

    await screen.findByRole('heading', { name: 'Pikachu MX' });
    expect(screen.queryByRole('button', { name: 'Restock Collection' })).not.toBeInTheDocument();
  });

  it('starts a restock, shows progress and refreshes the collection when it finishes', async () => {
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

    await user.click(await screen.findByRole('button', { name: 'Restock Collection' }));
    const dialog = await screen.findByRole('dialog', { name: 'Restock your collection' });
    const to = within(dialog).getByLabelText('To Pokédex #');
    await user.clear(to);
    await user.type(to, '3');
    await user.click(within(dialog).getByRole('button', { name: 'Start restock' }));

    expect(await within(dialog).findByLabelText('Restock progress')).toBeInTheDocument();
    expect(within(dialog).getByRole('button', { name: 'Start restock' })).toBeDisabled();
    expect(requested).toEqual({ fromId: 1, toId: 3 });

    expect(
      await screen.findByText(
        'Restock complete: 1 caught, 1 refreshed, 1 got away',
        {},
        { timeout: 4000 },
      ),
    ).toBeInTheDocument();
    expect(within(dialog).getByText('#3: no Pokémon with this Pokédex number')).toBeInTheDocument();
    expect(collectionLoads).toBeGreaterThan(1);
  });

  it('fills the range from a generation quick pick', async () => {
    let requested: SyncRangePayload | null = null;
    server.use(
      http.post(`${API}/admin/sync-jobs`, async ({ request }) => {
        requested = (await request.json()) as SyncRangePayload;
        return HttpResponse.json({ ...completedJob, fromId: 152, toId: 251 }, { status: 202 });
      }),
      http.get(`${API}/admin/sync-jobs/:id`, () =>
        HttpResponse.json({ ...completedJob, fromId: 152, toId: 251 }),
      ),
    );
    const { user } = renderApp('/collection', { as: ADMIN });

    await user.click(await screen.findByRole('button', { name: 'Restock Collection' }));
    const dialog = await screen.findByRole('dialog', { name: 'Restock your collection' });
    await user.click(within(dialog).getByRole('button', { name: 'Gen II (152–251)' }));

    expect(within(dialog).getByLabelText('From Pokédex #')).toHaveValue(152);
    expect(within(dialog).getByLabelText('To Pokédex #')).toHaveValue(251);
    await user.click(within(dialog).getByRole('button', { name: 'Start restock' }));

    await waitFor(() => expect(requested).toEqual({ fromId: 152, toId: 251 }));
  });

  it('validates the range on the client', async () => {
    const { user } = renderApp('/collection', { as: ADMIN });

    await user.click(await screen.findByRole('button', { name: 'Restock Collection' }));
    const dialog = await screen.findByRole('dialog', { name: 'Restock your collection' });
    const from = within(dialog).getByLabelText('From Pokédex #');
    await user.clear(from);
    await user.type(from, '200');
    await user.click(within(dialog).getByRole('button', { name: 'Start restock' }));

    expect(
      await within(dialog).findByText('Must be at or after the From number'),
    ).toBeInTheDocument();
  });

  it('explains that another restock is already running', async () => {
    server.use(
      http.post(`${API}/admin/sync-jobs`, () =>
        HttpResponse.json(
          {
            status: 409,
            title: 'Conflict',
            detail: 'A restock is already in progress. Hang tight until it finishes!',
          },
          { status: 409 },
        ),
      ),
    );
    const { user } = renderApp('/collection', { as: ADMIN });

    await user.click(await screen.findByRole('button', { name: 'Restock Collection' }));
    const dialog = await screen.findByRole('dialog', { name: 'Restock your collection' });
    await user.click(within(dialog).getByRole('button', { name: 'Start restock' }));

    expect(
      await within(dialog).findByText(
        'A restock is already in progress. Hang tight until it finishes!',
      ),
    ).toBeInTheDocument();
  });

  it('shows the result of the latest restock when reopened', async () => {
    server.use(
      http.get(`${API}/admin/sync-jobs/latest`, () => HttpResponse.json(completedJob)),
      http.get(`${API}/admin/sync-jobs/:id`, () => HttpResponse.json(completedJob)),
    );
    const { user } = renderApp('/collection', { as: ADMIN });

    await user.click(await screen.findByRole('button', { name: 'Restock Collection' }));
    const dialog = await screen.findByRole('dialog', { name: 'Restock your collection' });

    expect(
      await within(dialog).findByText('Restock complete, but 1 Pokémon got away.'),
    ).toBeInTheDocument();
    expect(within(dialog).getByText('Got away:')).toBeInTheDocument();
    expect(
      within(dialog).getByText('#1–#3: 1 caught · 1 refreshed · 1 got away'),
    ).toBeInTheDocument();
  });
});
