import { apiClient } from '../../shared/api/client';
import type { SyncJob, SyncRangePayload } from '../../shared/api/types';

const SYNC_JOBS = '/admin/sync-jobs';

export async function startSyncJob(range: SyncRangePayload): Promise<SyncJob> {
  const { data } = await apiClient.post<SyncJob>(SYNC_JOBS, range);
  return data;
}

export async function fetchSyncJob(id: string): Promise<SyncJob> {
  const { data } = await apiClient.get<SyncJob>(`${SYNC_JOBS}/${id}`);
  return data;
}

/** Resolves to null when no job has run since the backend started (204). */
export async function fetchLatestSyncJob(): Promise<SyncJob | null> {
  const { data, status } = await apiClient.get<SyncJob>(`${SYNC_JOBS}/latest`);
  return status === 204 ? null : data;
}
