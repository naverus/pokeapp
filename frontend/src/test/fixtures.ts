import type {
  LocalPokemon,
  PageResponse,
  PokemonDetail,
  PokemonSummary,
} from '../shared/api/types';

export const pikachuSummary: PokemonSummary = {
  id: 25,
  name: 'pikachu',
  spriteUrl: 'https://img/25.png',
  category: 'Mouse Pokémon',
  weightKg: 6,
  abilities: ['static', 'lightning-rod'],
  types: ['electric'],
};

export const bulbasaurSummary: PokemonSummary = {
  id: 1,
  name: 'bulbasaur',
  spriteUrl: 'https://img/1.png',
  category: 'Seed Pokémon',
  weightKg: 6.9,
  abilities: ['overgrow'],
  types: ['grass', 'poison'],
};

export const pikachuDetail: PokemonDetail = {
  id: 25,
  name: 'pikachu',
  imageUrl: 'https://img/artwork/25.png',
  category: 'Mouse Pokémon',
  heightM: 0.4,
  weightKg: 6,
  types: ['electric'],
  abilities: ['static', 'lightning-rod'],
  stats: [
    { name: 'hp', baseValue: 35 },
    { name: 'special-attack', baseValue: 50 },
  ],
  totalBaseStats: 85,
  description: 'When several of these Pokémon gather, their electricity could build.',
  evolutionChain: [
    { speciesId: 172, name: 'pichu', stage: 1, spriteUrl: null },
    { speciesId: 25, name: 'pikachu', stage: 2, spriteUrl: null },
    { speciesId: 26, name: 'raichu', stage: 3, spriteUrl: null },
  ],
};

export const storedPikachu: LocalPokemon = {
  id: 4,
  pokeApiId: 25,
  name: 'pikachu',
  localName: 'Pikachu MX',
  region: 'Kanto',
  category: 'Mouse Pokémon',
  heightM: 0.4,
  weightKg: 6,
  spriteUrl: null,
  description: 'Electric mouse',
  types: ['electric'],
  abilities: ['static'],
  tags: ['mascot'],
  version: 3,
  createdAt: '2026-01-01T00:00:00Z',
  updatedAt: '2026-01-01T00:00:00Z',
};

export function page<T>(
  content: T[],
  pageNumber = 0,
  size = 12,
  totalElements = content.length,
): PageResponse<T> {
  return {
    content,
    page: pageNumber,
    size,
    totalElements,
    totalPages: Math.ceil(totalElements / size),
  };
}
