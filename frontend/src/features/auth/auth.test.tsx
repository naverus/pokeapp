import { screen } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import { describe, expect, it } from 'vitest';
import { tokenStorage } from '../../shared/api/tokenStorage';
import { ASH, renderApp } from '../../test/renderApp';
import { API, server } from '../../test/server';

describe('Authentication', () => {
  it('redirects anonymous users from protected pages to the login page', async () => {
    renderApp('/collection');

    expect(await screen.findByRole('heading', { name: 'Sign in' })).toBeInTheDocument();
  });

  it('validates the form before calling the API', async () => {
    const { user } = renderApp('/login');

    await user.click(screen.getByRole('button', { name: 'Sign in' }));

    expect(await screen.findByText('Username is required')).toBeInTheDocument();
    expect(screen.getByText('Password is required')).toBeInTheDocument();
  });

  it('logs in, stores the session and returns to the requested page', async () => {
    const { user } = renderApp('/collection');

    await user.type(await screen.findByLabelText('Username'), 'ash');
    await user.type(screen.getByLabelText('Password'), 'pikachu123');
    await user.click(screen.getByRole('button', { name: 'Sign in' }));

    expect(await screen.findByRole('heading', { name: 'My collection' })).toBeInTheDocument();
    expect(screen.getByText('ash · USER')).toBeInTheDocument();
    expect(tokenStorage.load()?.token).toBe('jwt-token');
  });

  it('shows the server message for invalid credentials', async () => {
    const { user } = renderApp('/login');

    await user.type(screen.getByLabelText('Username'), 'ash');
    await user.type(screen.getByLabelText('Password'), 'wrong');
    await user.click(screen.getByRole('button', { name: 'Sign in' }));

    expect(await screen.findByText('Invalid username or password')).toBeInTheDocument();
  });

  it('registers a new user and signs them in', async () => {
    server.use(
      http.post(`${API}/auth/register`, () =>
        HttpResponse.json({ id: 9, username: 'misty', role: 'USER' }, { status: 201 }),
      ),
    );
    const { user } = renderApp('/register');

    await user.type(screen.getByLabelText('Username'), 'misty');
    await user.type(screen.getByLabelText('Password'), 'pikachu123');
    await user.click(screen.getByRole('button', { name: 'Register' }));

    expect(await screen.findByText('misty · USER')).toBeInTheDocument();
  });

  it('maps server field errors on registration', async () => {
    server.use(
      http.post(`${API}/auth/register`, () =>
        HttpResponse.json(
          { status: 409, detail: "Username 'ash' is already taken" },
          { status: 409 },
        ),
      ),
    );
    const { user } = renderApp('/register');

    await user.type(screen.getByLabelText('Username'), 'ash');
    await user.type(screen.getByLabelText('Password'), 'pikachu123');
    await user.click(screen.getByRole('button', { name: 'Register' }));

    expect(await screen.findByText("Username 'ash' is already taken")).toBeInTheDocument();
  });

  it('signs out', async () => {
    const { user } = renderApp('/', { as: ASH });

    await user.click(await screen.findByRole('button', { name: 'Sign out' }));

    expect(screen.getByRole('link', { name: 'Sign in' })).toBeInTheDocument();
    expect(tokenStorage.load()).toBeNull();
  });

  it('drops the session when the API rejects the token', async () => {
    server.use(
      http.get(`${API}/local-pokemon`, () =>
        HttpResponse.json(
          { status: 401, detail: 'A valid bearer token is required' },
          { status: 401 },
        ),
      ),
    );
    renderApp('/collection', { as: ASH });

    expect(await screen.findByRole('heading', { name: 'Sign in' })).toBeInTheDocument();
    expect(tokenStorage.load()).toBeNull();
  });
});
