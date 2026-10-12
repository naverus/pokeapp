import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { queryKeys } from '../../shared/api/queryKeys';
import type { CreateLocalPokemonPayload, UpdateLocalPokemonPayload } from '../../shared/api/types';
import {
  createLocalPokemon,
  deleteLocalPokemon,
  fetchLocalPokemon,
  fetchStoredPokeApiIds,
  updateLocalPokemon,
} from './api';

export function useLocalPokemonPage(page: number, size: number, name: string) {
  return useQuery({
    queryKey: queryKeys.localPage(page, size, name),
    queryFn: () => fetchLocalPokemon(page, size, name),
    placeholderData: keepPreviousData,
  });
}

/** Total number of Pokémon in the collection, ignoring any search filter. */
export function useCollectionSize(): number | undefined {
  const { data } = useQuery({
    queryKey: queryKeys.localPage(0, 1, ''),
    queryFn: () => fetchLocalPokemon(0, 1, ''),
    select: (result) => result.totalElements,
  });
  return data;
}

const NO_IDS: ReadonlySet<number> = new Set();

/** PokeAPI ids (of the given ones) already stored locally; empty while unknown or disabled. */
export function useStoredPokeApiIds(ids: number[], enabled: boolean): ReadonlySet<number> {
  const { data } = useQuery({
    queryKey: queryKeys.localStoredIds(ids),
    queryFn: () => fetchStoredPokeApiIds(ids),
    enabled: enabled && ids.length > 0,
    placeholderData: keepPreviousData,
    select: (stored) => new Set(stored),
  });
  return enabled && data ? data : NO_IDS;
}

function useInvalidateCollection() {
  const queryClient = useQueryClient();
  return () => queryClient.invalidateQueries({ queryKey: queryKeys.localAll });
}

export function useCreateLocalPokemon() {
  const invalidate = useInvalidateCollection();
  return useMutation({
    mutationFn: (payload: CreateLocalPokemonPayload) => createLocalPokemon(payload),
    onSuccess: invalidate,
  });
}

export function useUpdateLocalPokemon() {
  const invalidate = useInvalidateCollection();
  return useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: UpdateLocalPokemonPayload }) =>
      updateLocalPokemon(id, payload),
    onSuccess: invalidate,
  });
}

export function useDeleteLocalPokemon() {
  const invalidate = useInvalidateCollection();
  return useMutation({
    mutationFn: (id: number) => deleteLocalPokemon(id),
    onSuccess: invalidate,
  });
}
