export type Role = 'USER' | 'ADMIN';

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface PokemonSummary {
  id: number;
  name: string;
  spriteUrl: string | null;
  category: string | null;
  weightKg: number;
  abilities: string[];
  types: string[];
}

export interface Stat {
  name: string;
  baseValue: number;
}

export interface EvolutionStage {
  speciesId: number;
  name: string;
  stage: number;
  spriteUrl: string | null;
}

export interface PokemonDetail {
  id: number;
  name: string;
  imageUrl: string | null;
  category: string | null;
  heightM: number;
  weightKg: number;
  types: string[];
  abilities: string[];
  stats: Stat[];
  totalBaseStats: number;
  description: string | null;
  evolutionChain: EvolutionStage[];
}

export interface LocalPokemon {
  id: number;
  pokeApiId: number | null;
  name: string;
  localName: string | null;
  region: string | null;
  category: string | null;
  heightM: number;
  weightKg: number;
  spriteUrl: string | null;
  description: string | null;
  types: string[];
  abilities: string[];
  tags: string[];
  version: number;
  createdAt: string;
  updatedAt: string;
}

export interface LocalPokemonPayload {
  name: string;
  localName?: string | null;
  region?: string | null;
  category?: string | null;
  heightM: number;
  weightKg: number;
  spriteUrl?: string | null;
  description?: string | null;
  types: string[];
  abilities: string[];
  tags: string[];
}

export interface UpdateLocalPokemonPayload extends LocalPokemonPayload {
  version: number;
}

export interface CreateLocalPokemonPayload extends LocalPokemonPayload {
  pokeApiId?: number | null;
}

export type SyncJobStatus = 'RUNNING' | 'COMPLETED' | 'FAILED';

export interface SyncRangePayload {
  fromId: number;
  toId: number;
}

export interface SyncJob {
  id: string;
  status: SyncJobStatus;
  fromId: number;
  toId: number;
  total: number;
  processed: number;
  inserted: number;
  updated: number;
  failed: number;
  errors: string[];
  requestedBy: string;
  startedAt: string;
  finishedAt: string | null;
}

export interface TokenResponse {
  accessToken: string;
  tokenType: string;
  expiresAt: string;
  username: string;
  role: Role;
}

export interface UserResponse {
  id: number;
  username: string;
  role: Role;
}

export interface ProblemDetail {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  instance?: string;
  errors?: Record<string, string>;
}
