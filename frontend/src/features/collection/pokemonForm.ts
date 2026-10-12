import { z } from 'zod';
import type { LocalPokemon, LocalPokemonPayload } from '../../shared/api/types';

const optionalText = (max: number) => z.string().trim().max(max, `At most ${max} characters`);

const commaList = (maxItems: number, maxLength: number, label: string) =>
  z
    .string()
    .refine((value) => splitList(value).length <= maxItems, `At most ${maxItems} ${label}`)
    .refine(
      (value) => splitList(value).every((item) => item.length <= maxLength),
      `Each entry must be at most ${maxLength} characters`,
    );

export const pokemonFormSchema = z.object({
  name: z.string().trim().min(1, 'Name is required').max(100, 'At most 100 characters'),
  localName: optionalText(100),
  region: optionalText(50),
  category: optionalText(100),
  heightM: z.coerce
    .number({ invalid_type_error: 'Must be a number' })
    .min(0, 'Must be zero or positive')
    .max(100, 'At most 100 m'),
  weightKg: z.coerce
    .number({ invalid_type_error: 'Must be a number' })
    .min(0, 'Must be zero or positive')
    .max(10000, 'At most 10000 kg'),
  spriteUrl: optionalText(500),
  description: optionalText(1000),
  types: commaList(5, 30, 'types'),
  abilities: commaList(10, 50, 'abilities'),
  tags: commaList(10, 30, 'tags'),
});

export type PokemonFormInput = z.input<typeof pokemonFormSchema>;
export type PokemonFormValues = z.output<typeof pokemonFormSchema>;

export function splitList(value: string): string[] {
  return value
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean);
}

const emptyToNull = (value: string) => (value.trim() === '' ? null : value.trim());

export function toFormInput(pokemon?: LocalPokemon): PokemonFormInput {
  return {
    name: pokemon?.name ?? '',
    localName: pokemon?.localName ?? '',
    region: pokemon?.region ?? '',
    category: pokemon?.category ?? '',
    heightM: pokemon?.heightM ?? 0,
    weightKg: pokemon?.weightKg ?? 0,
    spriteUrl: pokemon?.spriteUrl ?? '',
    description: pokemon?.description ?? '',
    types: pokemon?.types.join(', ') ?? '',
    abilities: pokemon?.abilities.join(', ') ?? '',
    tags: pokemon?.tags.join(', ') ?? '',
  };
}

export function toPayload(values: PokemonFormValues): LocalPokemonPayload {
  return {
    name: values.name,
    localName: emptyToNull(values.localName),
    region: emptyToNull(values.region),
    category: emptyToNull(values.category),
    heightM: values.heightM,
    weightKg: values.weightKg,
    spriteUrl: emptyToNull(values.spriteUrl),
    description: emptyToNull(values.description),
    types: splitList(values.types),
    abilities: splitList(values.abilities),
    tags: splitList(values.tags),
  };
}
