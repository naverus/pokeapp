import { useCallback, useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Grid,
  InputAdornment,
  LinearProgress,
  Pagination,
  Snackbar,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import SearchIcon from '@mui/icons-material/Search';
import { Link as RouterLink } from 'react-router-dom';
import { errorMessage } from '../../shared/api/errors';
import type { LocalPokemon, SyncJob } from '../../shared/api/types';
import { ErrorState, Loading } from '../../shared/components/QueryState';
import { displayName } from '../../shared/format';
import { useAuth } from '../auth/useAuth';
import { SyncPokemonButton } from '../sync/SyncPokemonButton';
import { ConfirmDeleteDialog } from './ConfirmDeleteDialog';
import {
  useCreateLocalPokemon,
  useDeleteLocalPokemon,
  useLocalPokemonPage,
  useUpdateLocalPokemon,
} from './hooks';
import { LocalPokemonCard } from './LocalPokemonCard';
import { PokemonFormDialog } from './PokemonFormDialog';

const PAGE_SIZE = 12;

type FormState = { mode: 'create' } | { mode: 'edit'; pokemon: LocalPokemon } | null;

function useDebouncedValue(value: string, delayMs: number): string {
  const [debounced, setDebounced] = useState(value);
  useEffect(() => {
    const timer = setTimeout(() => setDebounced(value), delayMs);
    return () => clearTimeout(timer);
  }, [value, delayMs]);
  return debounced;
}

export function CollectionPage() {
  const { isAdmin } = useAuth();
  const [page, setPage] = useState(1);
  const [search, setSearch] = useState('');
  const name = useDebouncedValue(search.trim(), 300);
  const { data, error, isPending, isFetching, refetch } = useLocalPokemonPage(
    page - 1,
    PAGE_SIZE,
    name,
  );
  const createPokemon = useCreateLocalPokemon();
  const updatePokemon = useUpdateLocalPokemon();
  const deletePokemon = useDeleteLocalPokemon();
  const [form, setForm] = useState<FormState>(null);
  const [toDelete, setToDelete] = useState<LocalPokemon | null>(null);
  const [notice, setNotice] = useState<{
    severity: 'success' | 'warning' | 'error';
    message: string;
  } | null>(null);

  const notifySyncFinished = useCallback((job: SyncJob) => {
    const summary = `${job.inserted} new, ${job.updated} updated, ${job.failed} failed`;
    if (job.status === 'FAILED') {
      setNotice({ severity: 'error', message: `Sync failed after ${summary}` });
    } else {
      setNotice({
        severity: job.failed > 0 ? 'warning' : 'success',
        message: `Synced: ${summary}`,
      });
    }
  }, []);

  const confirmDelete = () => {
    if (!toDelete) return;
    deletePokemon.mutate(toDelete.id, {
      onSuccess: () => setNotice({ severity: 'success', message: 'Pokémon deleted' }),
      onError: (deleteError) =>
        setNotice({ severity: 'error', message: errorMessage(deleteError) }),
      onSettled: () => setToDelete(null),
    });
  };

  return (
    <Stack spacing={3}>
      <Stack
        direction={{ xs: 'column', md: 'row' }}
        justifyContent="space-between"
        alignItems={{ xs: 'stretch', md: 'center' }}
        spacing={2}
      >
        <Box>
          <Typography variant="h4" component="h1">
            My collection
          </Typography>
          <Typography color="text.secondary">
            Pokémon stored in the local database, enriched with your own fields
          </Typography>
        </Box>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
          <TextField
            size="small"
            placeholder="Search by name"
            value={search}
            onChange={(event) => {
              setSearch(event.target.value);
              setPage(1);
            }}
            slotProps={{
              htmlInput: { 'aria-label': 'Search by name' },
              input: {
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon fontSize="small" />
                  </InputAdornment>
                ),
              },
            }}
          />
          {isAdmin && <SyncPokemonButton onFinished={notifySyncFinished} />}
          {isAdmin && (
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => setForm({ mode: 'create' })}
            >
              New Pokémon
            </Button>
          )}
        </Stack>
      </Stack>

      {isFetching && !isPending && <LinearProgress aria-label="Refreshing" />}
      {isPending && <Loading label="Loading collection" />}
      {error && <ErrorState error={error} onRetry={() => void refetch()} />}

      {data && data.content.length === 0 && (
        <Alert severity="info">
          No Pokémon found. Open the <RouterLink to="/">Pokédex</RouterLink> and use “Add to my
          collection”.
        </Alert>
      )}

      {data && data.content.length > 0 && (
        <>
          <Grid container spacing={2}>
            {data.content.map((pokemon) => (
              <Grid key={pokemon.id} size={{ xs: 12, sm: 6, md: 4, lg: 3 }}>
                <LocalPokemonCard
                  pokemon={pokemon}
                  canDelete={isAdmin}
                  onEdit={(selected) => setForm({ mode: 'edit', pokemon: selected })}
                  onDelete={setToDelete}
                />
              </Grid>
            ))}
          </Grid>
          {data.totalPages > 1 && (
            <Box sx={{ display: 'flex', justifyContent: 'center' }}>
              <Pagination
                count={data.totalPages}
                page={page}
                onChange={(_, value) => setPage(value)}
                color="primary"
              />
            </Box>
          )}
        </>
      )}

      {form && (
        <PokemonFormDialog
          open
          key={form.mode === 'edit' ? `edit-${form.pokemon.id}-${form.pokemon.version}` : 'create'}
          pokemon={form.mode === 'edit' ? form.pokemon : undefined}
          onClose={() => setForm(null)}
          onSubmit={async (payload) => {
            if (form.mode === 'edit') {
              await updatePokemon.mutateAsync({
                id: form.pokemon.id,
                payload: { ...payload, version: form.pokemon.version },
              });
              setNotice({ severity: 'success', message: 'Pokémon updated' });
            } else {
              await createPokemon.mutateAsync(payload);
              setNotice({ severity: 'success', message: 'Pokémon created' });
            }
          }}
        />
      )}

      <ConfirmDeleteDialog
        name={toDelete ? (toDelete.localName ?? displayName(toDelete.name)) : null}
        pending={deletePokemon.isPending}
        onCancel={() => setToDelete(null)}
        onConfirm={confirmDelete}
      />

      {notice && (
        <Snackbar open autoHideDuration={4000} onClose={() => setNotice(null)}>
          <Alert severity={notice.severity} variant="filled" onClose={() => setNotice(null)}>
            {notice.message}
          </Alert>
        </Snackbar>
      )}
    </Stack>
  );
}
