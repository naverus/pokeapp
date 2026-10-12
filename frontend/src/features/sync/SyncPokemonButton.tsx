import { useState } from 'react';
import { Button, CircularProgress } from '@mui/material';
import SyncIcon from '@mui/icons-material/Sync';
import { errorMessage } from '../../shared/api/errors';
import type { SyncJob } from '../../shared/api/types';
import { useLatestSyncJob, useStartSync, useSyncJob } from './hooks';
import { SyncPokemonDialog } from './SyncPokemonDialog';

interface SyncPokemonButtonProps {
  onFinished: (job: SyncJob) => void;
}

/** Admin-only entry point for the on-demand bulk synchronization (US03). */
export function SyncPokemonButton({ onFinished }: SyncPokemonButtonProps) {
  const [open, setOpen] = useState(false);
  const [startedId, setStartedId] = useState<string | null>(null);
  const latest = useLatestSyncJob(true);
  const startSync = useStartSync();
  const trackedId = startedId ?? latest.data?.id ?? null;
  const { data: job } = useSyncJob(trackedId, onFinished);
  const running = job?.status === 'RUNNING';

  return (
    <>
      <Button
        variant="outlined"
        startIcon={running ? <CircularProgress size={18} /> : <SyncIcon />}
        onClick={() => setOpen(true)}
      >
        Sync Pokemon data
      </Button>
      {open && (
        <SyncPokemonDialog
          open
          job={job ?? null}
          starting={startSync.isPending}
          error={startSync.error ? errorMessage(startSync.error) : null}
          onStart={(range) =>
            startSync.mutate(range, { onSuccess: (started) => setStartedId(started.id) })
          }
          onClose={() => setOpen(false)}
        />
      )}
    </>
  );
}
