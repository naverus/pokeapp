import { AxiosError, AxiosHeaders } from 'axios';
import { describe, expect, it } from 'vitest';
import { splitList, toPayload } from '../features/collection/pokemonForm';
import { errorMessage, toProblem } from './api/errors';
import { tokenStorage } from './api/tokenStorage';
import { displayName, pokedexNumber } from './format';

function axiosError(status?: number, data?: unknown): AxiosError {
  const config = { headers: new AxiosHeaders() };
  const response =
    status === undefined ? undefined : { status, data, statusText: '', headers: {}, config };
  return new AxiosError('Request failed', 'ERR', config, null, response);
}

describe('errors', () => {
  it('reads problem details from API responses', () => {
    expect(toProblem(axiosError(404, { detail: 'Not here' }))).toEqual({
      status: 404,
      detail: 'Not here',
    });
  });

  it('handles responses without a body and network failures', () => {
    expect(toProblem(axiosError(502, '')).status).toBe(502);
    expect(errorMessage(axiosError())).toMatch(/could not be reached/);
    expect(errorMessage(new Error('boom'))).toBe('boom');
    expect(errorMessage('weird')).toBe('Unexpected error');
  });
});

describe('tokenStorage', () => {
  it('discards expired or corrupted sessions', () => {
    tokenStorage.save({
      token: 't',
      username: 'ash',
      role: 'USER',
      expiresAt: '2000-01-01T00:00:00Z',
    });
    expect(tokenStorage.load()).toBeNull();

    localStorage.setItem('pokeapp.session', '{not json');
    expect(tokenStorage.load()).toBeNull();
  });
});

describe('formatting', () => {
  it('formats names and pokedex numbers', () => {
    expect(displayName('mr-mime')).toBe('Mr Mime');
    expect(pokedexNumber(25)).toBe('#0025');
    expect(pokedexNumber(null)).toBe('Custom');
  });
});

describe('pokemon form mapping', () => {
  it('splits comma separated lists and nulls empty optional fields', () => {
    expect(splitList(' fire, ,flying ')).toEqual(['fire', 'flying']);
    expect(
      toPayload({
        name: 'charizard',
        localName: ' ',
        region: 'Kanto',
        category: '',
        heightM: 1.7,
        weightKg: 90.5,
        spriteUrl: '',
        description: '',
        types: 'fire, flying',
        abilities: '',
        tags: 'starter',
      }),
    ).toEqual({
      name: 'charizard',
      localName: null,
      region: 'Kanto',
      category: null,
      heightM: 1.7,
      weightKg: 90.5,
      spriteUrl: null,
      description: null,
      types: ['fire', 'flying'],
      abilities: [],
      tags: ['starter'],
    });
  });
});
