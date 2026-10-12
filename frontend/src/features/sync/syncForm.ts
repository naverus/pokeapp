import { z } from 'zod';

export const MAX_POKEAPI_ID = 1025;
export const MAX_RANGE_SIZE = 500;

const pokeApiId = z.coerce
  .number({ invalid_type_error: 'Must be a number' })
  .int('Must be a whole number')
  .min(1, 'Must be at least 1')
  .max(MAX_POKEAPI_ID, `Must be at most ${MAX_POKEAPI_ID}`);

export const syncFormSchema = z
  .object({ fromId: pokeApiId, toId: pokeApiId })
  .refine((range) => range.toId >= range.fromId, {
    path: ['toId'],
    message: 'Must be greater than or equal to From',
  })
  .refine((range) => range.toId - range.fromId < MAX_RANGE_SIZE, {
    path: ['toId'],
    message: `At most ${MAX_RANGE_SIZE} Pokémon per sync`,
  });

export type SyncFormInput = z.input<typeof syncFormSchema>;
export type SyncFormValues = z.output<typeof syncFormSchema>;

export const DEFAULT_SYNC_RANGE: SyncFormInput = { fromId: 1, toId: 151 };
