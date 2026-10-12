import { useEffect, useRef } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { queryKeys } from '../../shared/api/queryKeys';
import type { SyncJob, SyncRangePayload } from '../../shared/api/types';
import { fetchLatestSyncJob, fetchSyncJob, startSyncJob } from './api';

export const SYNC_POLL_INTERVAL_MS = 1000;

export function useLatestSyncJob(enabled: boolean) {
  return useQuery({
    queryKey: queryKeys.syncLatest,
    queryFn: fetchLatestSyncJob,
    enabled,
  });
}

export function useStartSync() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (range: SyncRangePayload) => startSyncJob(range),
    onSuccess: (job) => queryClient.setQueryData(queryKeys.syncJob(job.id), job),
  });
}

/**
 * Polls a job while it is RUNNING. When it finishes, the collection is refetched and
 * `onFinished` is called once.
 */
export function useSyncJob(id: string | null, onFinished?: (job: SyncJob) => void) {
  const queryClient = useQueryClient();
  const query = useQuery({
    queryKey: queryKeys.syncJob(id ?? ''),
    queryFn: () => fetchSyncJob(id ?? ''),
    enabled: id !== null,
    refetchInterval: (current) =>
      current.state.data?.status === 'RUNNING' ? SYNC_POLL_INTERVAL_MS : false,
  });

  const job = query.data;
  const previousStatus = useRef(job?.status);
  useEffect(() => {
    if (job && previousStatus.current === 'RUNNING' && job.status !== 'RUNNING') {
      void queryClient.invalidateQueries({ queryKey: queryKeys.localAll });
      onFinished?.(job);
    }
    previousStatus.current = job?.status;
  }, [job, queryClient, onFinished]);

  return query;
}
