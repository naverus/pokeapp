import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { queryKeys } from '../../shared/api/queryKeys';
import type { CreateLocalPokemonPayload, UpdateLocalPokemonPayload } from '../../shared/api/types';
import {
  createLocalPokemon,
  deleteLocalPokemon,
  fetchLocalPokemon,
  updateLocalPokemon,
} from './api';

export function useLocalPokemonPage(page: number, size: number, name: string) {
  return useQuery({
    queryKey: queryKeys.localPage(page, size, name),
    queryFn: () => fetchLocalPokemon(page, size, name),
    placeholderData: keepPreviousData,
  });
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
