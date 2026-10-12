import { screen, within } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import { describe, expect, it } from 'vitest';
import { bulbasaurSummary, page, pikachuSummary } from '../../test/fixtures';
import { renderApp } from '../../test/renderApp';
import { API, server } from '../../test/server';

describe('BrowsePage (US01)', () => {
  it('shows sprite, category, mass and abilities for each Pokemon', async () => {
    renderApp('/');

    const card = await screen.findByRole('link', { name: 'View Pikachu' });
    expect(within(card).getByRole('img', { name: 'Pikachu' })).toHaveAttribute(
      'src',
      'https://img/25.png',
    );
    expect(within(card).getByText('Mouse Pokémon · 6 kg')).toBeInTheDocument();
    expect(within(card).getByText(/Static, Lightning Rod/)).toBeInTheDocument();
    expect(within(card).getByText('electric')).toBeInTheDocument();
  });

  it('requests the next page when paginating', async () => {
    server.use(
      http.get(`${API}/pokemon`, ({ request }) => {
        const pageParam = Number(new URL(request.url).searchParams.get('page'));
        const content = pageParam === 0 ? [pikachuSummary] : [bulbasaurSummary];
        return HttpResponse.json(page(content, pageParam, 12, 24));
      }),
    );
    const { user } = renderApp('/');

    await screen.findByRole('link', { name: 'View Pikachu' });
    await user.click(screen.getByRole('button', { name: 'Go to page 2' }));

    expect(await screen.findByRole('link', { name: 'View Bulbasaur' })).toBeInTheDocument();
  });

  it('shows an error with retry when PokeAPI is unavailable', async () => {
    server.use(
      http.get(`${API}/pokemon`, () =>
        HttpResponse.json(
          { status: 503, detail: 'PokeAPI is currently unavailable' },
          { status: 503 },
        ),
      ),
    );
    renderApp('/');

    expect(await screen.findByText('PokeAPI is currently unavailable')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Retry' })).toBeInTheDocument();
  });
});
