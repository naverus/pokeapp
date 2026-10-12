import { screen } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import { describe, expect, it } from 'vitest';
import { storedPikachu } from '../../test/fixtures';
import { ASH, renderApp } from '../../test/renderApp';
import { API, server } from '../../test/server';

describe('DetailPage (US02 + US03)', () => {
  it('shows image, stats, description and evolution chain', async () => {
    renderApp('/pokemon/pikachu');

    expect(await screen.findByRole('heading', { name: 'Pikachu', level: 1 })).toBeInTheDocument();
    expect(screen.getByRole('img', { name: 'Pikachu' })).toHaveAttribute(
      'src',
      'https://img/artwork/25.png',
    );
    expect(screen.getByRole('progressbar', { name: 'Special Attack 50' })).toBeInTheDocument();
    expect(screen.getByText(/their electricity could build/)).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Raichu/ })).toHaveAttribute('href', '/pokemon/raichu');
  });

  it('sends anonymous users to the login page when adding to the collection', async () => {
    const { user } = renderApp('/pokemon/pikachu');

    await user.click(await screen.findByRole('button', { name: 'Sign in to add to collection' }));

    expect(await screen.findByRole('heading', { name: 'Sign in' })).toBeInTheDocument();
  });

  it('syncs the Pokemon into the local collection', async () => {
    let synced = '';
    server.use(
      http.post(`${API}/local-pokemon/sync/:idOrName`, ({ params, request }) => {
        synced = `${String(params.idOrName)} ${request.headers.get('Authorization')}`;
        return HttpResponse.json(storedPikachu, { status: 201 });
      }),
    );
    const { user } = renderApp('/pokemon/pikachu', { as: ASH });

    await user.click(await screen.findByRole('button', { name: 'Add to my collection' }));

    expect(await screen.findByText('Pikachu added to your collection')).toBeInTheDocument();
    expect(synced).toBe('pikachu Bearer jwt-token');
  });

  it('explains when the Pokemon is already synced', async () => {
    server.use(
      http.post(`${API}/local-pokemon/sync/:idOrName`, () =>
        HttpResponse.json(
          { status: 409, detail: 'Pokemon with PokeAPI id 25 is already stored locally' },
          { status: 409 },
        ),
      ),
    );
    const { user } = renderApp('/pokemon/pikachu', { as: ASH });

    await user.click(await screen.findByRole('button', { name: 'Add to my collection' }));

    expect(
      await screen.findByText('Pokemon with PokeAPI id 25 is already stored locally'),
    ).toBeInTheDocument();
  });

  it('shows not found errors', async () => {
    server.use(
      http.get(`${API}/pokemon/:idOrName`, () =>
        HttpResponse.json(
          { status: 404, detail: "Pokemon 'agumon' was not found in PokeAPI" },
          { status: 404 },
        ),
      ),
    );
    renderApp('/pokemon/agumon');

    expect(
      await screen.findByText("Pokemon 'agumon' was not found in PokeAPI"),
    ).toBeInTheDocument();
  });
});
