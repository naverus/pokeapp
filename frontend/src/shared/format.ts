export function displayName(name: string): string {
  return name
    .split('-')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ');
}

export function pokedexNumber(id: number | null | undefined): string {
  return id == null ? 'Custom' : `#${String(id).padStart(4, '0')}`;
}
