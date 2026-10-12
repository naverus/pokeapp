import {
  Box,
  Card,
  CardActions,
  CardContent,
  CardMedia,
  Chip,
  IconButton,
  Stack,
  Tooltip,
  Typography,
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import PlaceIcon from '@mui/icons-material/Place';
import type { LocalPokemon } from '../../shared/api/types';
import { TypeChips } from '../../shared/components/TypeChips';
import { displayName, pokedexNumber } from '../../shared/format';

interface LocalPokemonCardProps {
  pokemon: LocalPokemon;
  canDelete: boolean;
  onEdit: (pokemon: LocalPokemon) => void;
  onDelete: (pokemon: LocalPokemon) => void;
}

export function LocalPokemonCard({ pokemon, canDelete, onEdit, onDelete }: LocalPokemonCardProps) {
  const title = pokemon.localName ?? displayName(pokemon.name);

  return (
    <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <Box sx={{ bgcolor: 'grey.100', display: 'flex', justifyContent: 'center' }}>
        <CardMedia
          component="img"
          image={pokemon.spriteUrl ?? undefined}
          alt={title}
          loading="lazy"
          sx={{ width: 140, height: 140, objectFit: 'contain' }}
        />
      </Box>
      <CardContent sx={{ flexGrow: 1 }}>
        <Typography variant="caption" color="text.secondary">
          {pokedexNumber(pokemon.pokeApiId)}
        </Typography>
        <Typography variant="h6" component="h2">
          {title}
        </Typography>
        {pokemon.localName && (
          <Typography variant="body2" color="text.secondary">
            {displayName(pokemon.name)}
          </Typography>
        )}
        <Stack spacing={1} sx={{ mt: 1 }}>
          <TypeChips types={pokemon.types} />
          {pokemon.region && (
            <Stack direction="row" spacing={0.5} alignItems="center">
              <PlaceIcon fontSize="small" color="action" aria-hidden />
              <Typography variant="body2">{pokemon.region}</Typography>
            </Stack>
          )}
          <Stack direction="row" spacing={0.5} useFlexGap flexWrap="wrap">
            {pokemon.tags.map((tag) => (
              <Chip key={tag} label={`#${tag}`} size="small" variant="outlined" />
            ))}
          </Stack>
        </Stack>
      </CardContent>
      <CardActions sx={{ justifyContent: 'flex-end' }}>
        <Tooltip title="Edit">
          <IconButton aria-label={`Edit ${title}`} onClick={() => onEdit(pokemon)}>
            <EditIcon />
          </IconButton>
        </Tooltip>
        {canDelete && (
          <Tooltip title="Delete">
            <IconButton
              aria-label={`Delete ${title}`}
              color="error"
              onClick={() => onDelete(pokemon)}
            >
              <DeleteIcon />
            </IconButton>
          </Tooltip>
        )}
      </CardActions>
    </Card>
  );
}
