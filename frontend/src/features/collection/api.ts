import { apiClient } from '../../shared/api/client';
import type {
  CreateLocalPokemonPayload,
  LocalPokemon,
  PageResponse,
  UpdateLocalPokemonPayload,
} from '../../shared/api/types';

export async function fetchLocalPokemon(
  page: number,
  size: number,
  name: string,
): Promise<PageResponse<LocalPokemon>> {
  const { data } = await apiClient.get<PageResponse<LocalPokemon>>('/local-pokemon', {
    params: { page, size, ...(name ? { name } : {}) },
  });
  return data;
}

export async function createLocalPokemon(
  payload: CreateLocalPokemonPayload,
): Promise<LocalPokemon> {
  const { data } = await apiClient.post<LocalPokemon>('/local-pokemon', payload);
  return data;
}

export async function updateLocalPokemon(
  id: number,
  payload: UpdateLocalPokemonPayload,
): Promise<LocalPokemon> {
  const { data } = await apiClient.put<LocalPokemon>(`/local-pokemon/${id}`, payload);
  return data;
}

export async function fetchStoredPokeApiIds(ids: number[]): Promise<number[]> {
  const { data } = await apiClient.get<{ pokeApiIds: number[] }>('/local-pokemon/stored-ids', {
    params: { pokeApiIds: ids.join(',') },
  });
  return data.pokeApiIds;
}

export async function deleteLocalPokemon(id: number): Promise<void> {
  await apiClient.delete(`/local-pokemon/${id}`);
}
