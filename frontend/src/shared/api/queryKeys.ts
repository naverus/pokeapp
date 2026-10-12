export const queryKeys = {
  catalogPage: (page: number, size: number) => ['catalog', 'page', page, size] as const,
  catalogDetail: (idOrName: string) => ['catalog', 'detail', idOrName] as const,
  localAll: ['local-pokemon'] as const,
  localPage: (page: number, size: number, name: string) =>
    ['local-pokemon', 'page', page, size, name] as const,
  syncLatest: ['sync-jobs', 'latest'] as const,
  syncJob: (id: string) => ['sync-jobs', 'job', id] as const,
};
