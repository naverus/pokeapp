import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Grid,
  TextField,
} from '@mui/material';
import { errorMessage, toProblem } from '../../shared/api/errors';
import type { LocalPokemon, LocalPokemonPayload } from '../../shared/api/types';
import {
  pokemonFormSchema,
  toFormInput,
  toPayload,
  type PokemonFormInput,
  type PokemonFormValues,
} from './pokemonForm';

interface PokemonFormDialogProps {
  open: boolean;
  pokemon?: LocalPokemon;
  onClose: () => void;
  onSubmit: (payload: LocalPokemonPayload) => Promise<unknown>;
}

type FieldName = keyof PokemonFormInput;

const FIELDS: {
  name: FieldName;
  label: string;
  type?: string;
  multiline?: boolean;
  helper?: string;
  half?: boolean;
}[] = [
  { name: 'name', label: 'Name', half: true },
  { name: 'localName', label: 'Local name', half: true, helper: 'Localized nomenclature' },
  { name: 'region', label: 'Region', half: true, helper: 'Geographical metadata' },
  { name: 'category', label: 'Category', half: true },
  { name: 'heightM', label: 'Height (m)', type: 'number', half: true },
  { name: 'weightKg', label: 'Weight (kg)', type: 'number', half: true },
  { name: 'types', label: 'Types', helper: 'Comma separated, e.g. fire, flying', half: true },
  { name: 'abilities', label: 'Abilities', helper: 'Comma separated', half: true },
  { name: 'tags', label: 'Tags', helper: 'Internal classification, comma separated' },
  { name: 'spriteUrl', label: 'Image URL' },
  { name: 'description', label: 'Description', multiline: true },
];

export function PokemonFormDialog({ open, pokemon, onClose, onSubmit }: PokemonFormDialogProps) {
  const [serverError, setServerError] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<PokemonFormInput, unknown, PokemonFormValues>({
    resolver: zodResolver(pokemonFormSchema),
    defaultValues: toFormInput(pokemon),
  });

  const submit = handleSubmit(async (values) => {
    setServerError(null);
    try {
      await onSubmit(toPayload(values));
      onClose();
    } catch (error) {
      const problem = toProblem(error);
      Object.entries(problem.errors ?? {}).forEach(([field, message]) => {
        if (FIELDS.some((candidate) => candidate.name === field)) {
          setError(field as FieldName, { message });
        }
      });
      setServerError(
        problem.status === 409
          ? `${errorMessage(error)}. Close the dialog to reload the latest data.`
          : errorMessage(error),
      );
    }
  });

  const title = pokemon ? 'Edit Pokémon' : 'Create Pokémon';

  return (
    <Dialog
      open={open}
      onClose={onClose}
      fullWidth
      maxWidth="md"
      aria-labelledby="pokemon-form-title"
    >
      <form onSubmit={submit} noValidate>
        <DialogTitle id="pokemon-form-title">{title}</DialogTitle>
        <DialogContent dividers>
          {serverError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {serverError}
            </Alert>
          )}
          <Grid container spacing={2}>
            {FIELDS.map((field) => (
              <Grid key={field.name} size={{ xs: 12, sm: field.half ? 6 : 12 }}>
                <TextField
                  fullWidth
                  label={field.label}
                  type={field.type ?? 'text'}
                  multiline={field.multiline}
                  minRows={field.multiline ? 3 : undefined}
                  slotProps={
                    field.type === 'number' ? { htmlInput: { step: '0.1', min: 0 } } : undefined
                  }
                  {...register(field.name)}
                  error={Boolean(errors[field.name])}
                  helperText={errors[field.name]?.message ?? field.helper}
                />
              </Grid>
            ))}
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose}>Cancel</Button>
          <Button type="submit" variant="contained" disabled={isSubmitting}>
            Save
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}
