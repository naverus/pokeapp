import {
  Box,
  Card,
  CardActionArea,
  CardContent,
  CardMedia,
  Stack,
  Typography,
} from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';
import type { PokemonSummary } from '../../shared/api/types';
import { TypeChips } from '../../shared/components/TypeChips';
import { displayName, pokedexNumber } from '../../shared/format';

export function PokemonCard({ pokemon }: { pokemon: PokemonSummary }) {
  return (
    <Card sx={{ height: '100%' }}>
      <CardActionArea
        component={RouterLink}
        to={`/pokemon/${pokemon.name}`}
        sx={{ height: '100%' }}
        aria-label={`View ${displayName(pokemon.name)}`}
      >
        <Box sx={{ bgcolor: 'grey.100', display: 'flex', justifyContent: 'center' }}>
          <CardMedia
            component="img"
            image={pokemon.spriteUrl ?? undefined}
            alt={displayName(pokemon.name)}
            loading="lazy"
            sx={{ width: 120, height: 120, objectFit: 'contain', imageRendering: 'pixelated' }}
          />
        </Box>
        <CardContent>
          <Typography variant="caption" color="text.secondary">
            {pokedexNumber(pokemon.id)}
          </Typography>
          <Typography variant="h6" component="h2" gutterBottom>
            {displayName(pokemon.name)}
          </Typography>
          <Stack spacing={1}>
            <TypeChips types={pokemon.types} />
            <Typography variant="body2" color="text.secondary">
              {pokemon.category ?? 'Unknown category'} · {pokemon.weightKg} kg
            </Typography>
            <Typography variant="body2">
              <strong>Abilities:</strong> {pokemon.abilities.map(displayName).join(', ')}
            </Typography>
          </Stack>
        </CardContent>
      </CardActionArea>
    </Card>
  );
}
