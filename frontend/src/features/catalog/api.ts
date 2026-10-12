import { apiClient } from '../../shared/api/client';
import type {
  LocalPokemon,
  PageResponse,
  PokemonDetail,
  PokemonSummary,
} from '../../shared/api/types';

export async function fetchCatalogPage(
  page: number,
  size: number,
): Promise<PageResponse<PokemonSummary>> {
  const { data } = await apiClient.get<PageResponse<PokemonSummary>>('/pokemon', {
    params: { page, size },
  });
  return data;
}

export async function fetchPokemonDetail(idOrName: string): Promise<PokemonDetail> {
  const { data } = await apiClient.get<PokemonDetail>(`/pokemon/${encodeURIComponent(idOrName)}`);
  return data;
}

export async function syncPokemon(idOrName: string): Promise<LocalPokemon> {
  const { data } = await apiClient.post<LocalPokemon>(
    `/local-pokemon/sync/${encodeURIComponent(idOrName)}`,
  );
  return data;
}
