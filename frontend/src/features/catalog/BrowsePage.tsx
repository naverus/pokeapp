import {
  Box,
  FormControl,
  Grid,
  InputLabel,
  LinearProgress,
  MenuItem,
  Pagination,
  Select,
  Stack,
  Typography,
} from '@mui/material';
import { useSearchParams } from 'react-router-dom';
import { ErrorState, Loading } from '../../shared/components/QueryState';
import { useCatalogPage } from './hooks';
import { PokemonCard } from './PokemonCard';

const PAGE_SIZES = [12, 24, 48];

function readPositiveInt(value: string | null, fallback: number): number {
  const parsed = Number(value);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : fallback;
}

export function BrowsePage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const page = readPositiveInt(searchParams.get('page'), 1);
  const size = PAGE_SIZES.includes(Number(searchParams.get('size')))
    ? Number(searchParams.get('size'))
    : PAGE_SIZES[0];
  const { data, error, isPending, isFetching, refetch } = useCatalogPage(page - 1, size);

  const updateParams = (nextPage: number, nextSize: number) =>
    setSearchParams({ page: String(nextPage), size: String(nextSize) });

  return (
    <Stack spacing={3}>
      <Stack
        direction={{ xs: 'column', sm: 'row' }}
        justifyContent="space-between"
        alignItems={{ xs: 'flex-start', sm: 'center' }}
        spacing={2}
      >
        <Box>
          <Typography variant="h4" component="h1">
            Pokédex
          </Typography>
          <Typography color="text.secondary">
            Live data from PokeAPI{data ? ` · ${data.totalElements} Pokémon` : ''}
          </Typography>
        </Box>
        <FormControl size="small" sx={{ minWidth: 120 }}>
          <InputLabel id="page-size-label">Per page</InputLabel>
          <Select
            labelId="page-size-label"
            label="Per page"
            value={size}
            onChange={(event) => updateParams(1, Number(event.target.value))}
          >
            {PAGE_SIZES.map((option) => (
              <MenuItem key={option} value={option}>
                {option}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      </Stack>

      {isFetching && !isPending && <LinearProgress aria-label="Refreshing" />}
      {isPending && <Loading label="Loading Pokémon" />}
      {error && <ErrorState error={error} onRetry={() => void refetch()} />}

      {data && (
        <>
          <Grid container spacing={2}>
            {data.content.map((pokemon) => (
              <Grid key={pokemon.id} size={{ xs: 12, sm: 6, md: 4, lg: 3 }}>
                <PokemonCard pokemon={pokemon} />
              </Grid>
            ))}
          </Grid>
          <Box sx={{ display: 'flex', justifyContent: 'center' }}>
            <Pagination
              count={data.totalPages}
              page={page}
              onChange={(_, value) => updateParams(value, size)}
              color="primary"
              siblingCount={1}
            />
          </Box>
        </>
      )}
    </Stack>
  );
}
