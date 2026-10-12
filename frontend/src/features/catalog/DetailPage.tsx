import { useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Grid,
  Snackbar,
  Stack,
  Typography,
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import AddCircleIcon from '@mui/icons-material/AddCircle';
import { Link as RouterLink, useNavigate, useParams } from 'react-router-dom';
import { errorMessage } from '../../shared/api/errors';
import { ErrorState, Loading } from '../../shared/components/QueryState';
import { TypeChips } from '../../shared/components/TypeChips';
import { displayName, pokedexNumber } from '../../shared/format';
import { useAuth } from '../auth/useAuth';
import { EvolutionChain } from './EvolutionChain';
import { usePokemonDetail, useSyncPokemon } from './hooks';
import { StatBars } from './StatBars';

export function DetailPage() {
  const { idOrName = '' } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const { data: pokemon, error, isPending, refetch } = usePokemonDetail(idOrName);
  const sync = useSyncPokemon();
  const [feedback, setFeedback] = useState<{
    severity: 'success' | 'error';
    message: string;
  } | null>(null);

  const handleSync = () => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: `/pokemon/${idOrName}` } });
      return;
    }
    sync.mutate(idOrName, {
      onSuccess: (stored) =>
        setFeedback({
          severity: 'success',
          message: `${displayName(stored.name)} added to your collection`,
        }),
      onError: (syncError) => setFeedback({ severity: 'error', message: errorMessage(syncError) }),
    });
  };

  if (isPending) return <Loading label="Loading Pokémon" />;
  if (error) return <ErrorState error={error} onRetry={() => void refetch()} />;

  return (
    <Stack spacing={3}>
      <Box>
        <Button component={RouterLink} to="/" startIcon={<ArrowBackIcon />}>
          Back to Pokédex
        </Button>
      </Box>
      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 5 }}>
          <Card>
            <CardContent>
              <Box
                component="img"
                src={pokemon.imageUrl ?? undefined}
                alt={displayName(pokemon.name)}
                sx={{ width: '100%', maxHeight: 360, objectFit: 'contain' }}
              />
              <Typography variant="caption" color="text.secondary">
                {pokedexNumber(pokemon.id)}
              </Typography>
              <Typography variant="h4" component="h1">
                {displayName(pokemon.name)}
              </Typography>
              <Typography color="text.secondary" gutterBottom>
                {pokemon.category}
              </Typography>
              <TypeChips types={pokemon.types} size="medium" />
              <Stack direction="row" spacing={3} sx={{ mt: 2 }}>
                <Typography>
                  <strong>Height:</strong> {pokemon.heightM} m
                </Typography>
                <Typography>
                  <strong>Weight:</strong> {pokemon.weightKg} kg
                </Typography>
              </Stack>
              <Button
                variant="contained"
                startIcon={<AddCircleIcon />}
                sx={{ mt: 2 }}
                fullWidth
                onClick={handleSync}
                disabled={sync.isPending}
              >
                {isAuthenticated ? 'Add to my collection' : 'Sign in to add to collection'}
              </Button>
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, md: 7 }}>
          <Stack spacing={3}>
            <Card>
              <CardContent>
                <Typography variant="h6" component="h2" gutterBottom>
                  Description
                </Typography>
                <Typography>{pokemon.description ?? 'No description available.'}</Typography>
                <Typography variant="body2" sx={{ mt: 1 }}>
                  <strong>Abilities:</strong> {pokemon.abilities.map(displayName).join(', ')}
                </Typography>
              </CardContent>
            </Card>
            <Card>
              <CardContent>
                <Typography variant="h6" component="h2" gutterBottom>
                  Base stats
                </Typography>
                <StatBars stats={pokemon.stats} total={pokemon.totalBaseStats} />
              </CardContent>
            </Card>
            <Card>
              <CardContent>
                <Typography variant="h6" component="h2" gutterBottom>
                  Evolution chain
                </Typography>
                <EvolutionChain stages={pokemon.evolutionChain} currentName={pokemon.name} />
              </CardContent>
            </Card>
          </Stack>
        </Grid>
      </Grid>
      {feedback && (
        <Snackbar
          open
          autoHideDuration={4000}
          onClose={() => setFeedback(null)}
          anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
        >
          <Alert severity={feedback.severity} onClose={() => setFeedback(null)} variant="filled">
            {feedback.message}
          </Alert>
        </Snackbar>
      )}
    </Stack>
  );
}
