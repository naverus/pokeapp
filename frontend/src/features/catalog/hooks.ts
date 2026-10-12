import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { queryKeys } from '../../shared/api/queryKeys';
import { fetchCatalogPage, fetchPokemonDetail, syncPokemon } from './api';

const FIVE_MINUTES = 5 * 60 * 1000;

export function useCatalogPage(page: number, size: number) {
  return useQuery({
    queryKey: queryKeys.catalogPage(page, size),
    queryFn: () => fetchCatalogPage(page, size),
    placeholderData: keepPreviousData,
    staleTime: FIVE_MINUTES,
  });
}

export function usePokemonDetail(idOrName: string) {
  return useQuery({
    queryKey: queryKeys.catalogDetail(idOrName),
    queryFn: () => fetchPokemonDetail(idOrName),
    staleTime: FIVE_MINUTES,
  });
}

export function useSyncPokemon() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: syncPokemon,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.localAll }),
  });
}
