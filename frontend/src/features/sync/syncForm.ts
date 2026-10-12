import { z } from 'zod';

export const MAX_POKEAPI_ID = 1025;
export const MAX_RANGE_SIZE = 500;

const pokeApiId = z.coerce
  .number({ invalid_type_error: 'Enter a Pokédex number' })
  .int('Use a whole number')
  .min(1, 'Pokédex numbers start at 1')
  .max(MAX_POKEAPI_ID, `The highest Pokédex number is ${MAX_POKEAPI_ID}`);

export const syncFormSchema = z
  .object({ fromId: pokeApiId, toId: pokeApiId })
  .refine((range) => range.toId >= range.fromId, {
    path: ['toId'],
    message: 'Must be at or after the From number',
  })
  .refine((range) => range.toId - range.fromId < MAX_RANGE_SIZE, {
    path: ['toId'],
    message: `That's a lot! Up to ${MAX_RANGE_SIZE} Pokémon per restock`,
  });

export type SyncFormInput = z.input<typeof syncFormSchema>;
export type SyncFormValues = z.output<typeof syncFormSchema>;

export const DEFAULT_SYNC_RANGE: SyncFormInput = { fromId: 1, toId: 151 };
