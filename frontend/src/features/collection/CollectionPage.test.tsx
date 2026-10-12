import { screen, within } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import { describe, expect, it } from 'vitest';
import type { UpdateLocalPokemonPayload } from '../../shared/api/types';
import { page, storedPikachu } from '../../test/fixtures';
import { ADMIN, ASH, renderApp } from '../../test/renderApp';
import { API, server } from '../../test/server';

describe('CollectionPage (CRUD + US04)', () => {
  it('lists local Pokemon with custom fields and hides admin actions for users', async () => {
    renderApp('/collection', { as: ASH });

    expect(await screen.findByRole('heading', { name: 'Pikachu MX' })).toBeInTheDocument();
    expect(screen.getByText('Kanto')).toBeInTheDocument();
    expect(screen.getByText('#mascot')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Edit Pikachu MX' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Delete Pikachu MX' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'New Pokémon' })).not.toBeInTheDocument();
  });

  it('filters by name', async () => {
    let lastName: string | null = null;
    server.use(
      http.get(`${API}/local-pokemon`, ({ request }) => {
        lastName = new URL(request.url).searchParams.get('name');
        return HttpResponse.json(page(lastName ? [] : [storedPikachu]));
      }),
    );
    const { user } = renderApp('/collection', { as: ASH });

    await screen.findByRole('heading', { name: 'Pikachu MX' });
    await user.type(screen.getByRole('textbox', { name: 'Search by name' }), 'zz');

    expect(await screen.findByText(/No Pokémon found/)).toBeInTheDocument();
    expect(lastName).toBe('zz');
  });

  it('updates a Pokemon sending the version and list fields', async () => {
    let received: UpdateLocalPokemonPayload | null = null;
    server.use(
      http.put(`${API}/local-pokemon/:id`, async ({ request }) => {
        received = (await request.json()) as UpdateLocalPokemonPayload;
        return HttpResponse.json({ ...storedPikachu, ...received, version: 4 });
      }),
    );
    const { user } = renderApp('/collection', { as: ASH });

    await user.click(await screen.findByRole('button', { name: 'Edit Pikachu MX' }));
    const dialog = await screen.findByRole('dialog', { name: 'Edit Pokémon' });
    const tags = within(dialog).getByLabelText('Tags');
    await user.clear(tags);
    await user.type(tags, 'mascot, starter');
    await user.click(within(dialog).getByRole('button', { name: 'Save' }));

    expect(await screen.findByText('Pokémon updated')).toBeInTheDocument();
    expect(received).toMatchObject({
      version: 3,
      name: 'pikachu',
      localName: 'Pikachu MX',
      tags: ['mascot', 'starter'],
      types: ['electric'],
    });
  });

  it('validates the form on the client', async () => {
    const { user } = renderApp('/collection', { as: ASH });

    await user.click(await screen.findByRole('button', { name: 'Edit Pikachu MX' }));
    const dialog = await screen.findByRole('dialog', { name: 'Edit Pokémon' });
    await user.clear(within(dialog).getByLabelText('Name'));
    await user.click(within(dialog).getByRole('button', { name: 'Save' }));

    expect(await within(dialog).findByText('Name is required')).toBeInTheDocument();
  });

  it('shows server-side validation errors next to the fields', async () => {
    server.use(
      http.put(`${API}/local-pokemon/:id`, () =>
        HttpResponse.json(
          {
            status: 400,
            title: 'Validation failed',
            detail: 'The request contains invalid data',
            errors: { region: 'must be at most 50 characters' },
          },
          { status: 400 },
        ),
      ),
    );
    const { user } = renderApp('/collection', { as: ASH });

    await user.click(await screen.findByRole('button', { name: 'Edit Pikachu MX' }));
    const dialog = await screen.findByRole('dialog', { name: 'Edit Pokémon' });
    await user.click(within(dialog).getByRole('button', { name: 'Save' }));

    expect(await within(dialog).findByText('must be at most 50 characters')).toBeInTheDocument();
    expect(within(dialog).getByText('The request contains invalid data')).toBeInTheDocument();
  });

  it('explains optimistic locking conflicts', async () => {
    server.use(
      http.put(`${API}/local-pokemon/:id`, () =>
        HttpResponse.json(
          { status: 409, detail: 'Pokemon 4 was modified by someone else' },
          { status: 409 },
        ),
      ),
    );
    const { user } = renderApp('/collection', { as: ASH });

    await user.click(await screen.findByRole('button', { name: 'Edit Pikachu MX' }));
    const dialog = await screen.findByRole('dialog', { name: 'Edit Pokémon' });
    await user.click(within(dialog).getByRole('button', { name: 'Save' }));

    expect(
      await within(dialog).findByText(/modified by someone else\. Close the dialog/),
    ).toBeInTheDocument();
  });

  it('lets admins delete after confirmation', async () => {
    let deletedId = '';
    server.use(
      http.delete(`${API}/local-pokemon/:id`, ({ params }) => {
        deletedId = String(params.id);
        return new HttpResponse(null, { status: 204 });
      }),
    );
    const { user } = renderApp('/collection', { as: ADMIN });

    await user.click(await screen.findByRole('button', { name: 'Delete Pikachu MX' }));
    const dialog = await screen.findByRole('dialog', { name: 'Delete Pokémon' });
    await user.click(within(dialog).getByRole('button', { name: 'Delete' }));

    expect(await screen.findByText('Pokémon deleted')).toBeInTheDocument();
    expect(deletedId).toBe('4');
  });

  it('lets admins create a custom Pokemon', async () => {
    let created: Record<string, unknown> | null = null;
    server.use(
      http.post(`${API}/local-pokemon`, async ({ request }) => {
        created = (await request.json()) as Record<string, unknown>;
        return HttpResponse.json({ ...storedPikachu, id: 99 }, { status: 201 });
      }),
    );
    const { user } = renderApp('/collection', { as: ADMIN });

    await user.click(await screen.findByRole('button', { name: 'New Pokémon' }));
    const dialog = await screen.findByRole('dialog', { name: 'Create Pokémon' });
    await user.type(within(dialog).getByLabelText('Name'), 'missingno');
    await user.clear(within(dialog).getByLabelText('Weight (kg)'));
    await user.type(within(dialog).getByLabelText('Weight (kg)'), '1590.8');
    await user.click(within(dialog).getByRole('button', { name: 'Save' }));

    expect(await screen.findByText('Pokémon created')).toBeInTheDocument();
    expect(created).toMatchObject({
      name: 'missingno',
      weightKg: 1590.8,
      localName: null,
      tags: [],
    });
  });
});
