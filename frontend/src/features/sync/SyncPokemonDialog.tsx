import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  Alert,
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Grid,
  LinearProgress,
  TextField,
  Typography,
} from '@mui/material';
import type { SyncJob, SyncRangePayload } from '../../shared/api/types';
import {
  DEFAULT_SYNC_RANGE,
  MAX_POKEAPI_ID,
  syncFormSchema,
  type SyncFormInput,
  type SyncFormValues,
} from './syncForm';

interface SyncPokemonDialogProps {
  open: boolean;
  job: SyncJob | null;
  starting: boolean;
  error: string | null;
  onStart: (range: SyncRangePayload) => void;
  onClose: () => void;
}

function counters(job: SyncJob): string {
  return `${job.inserted} new, ${job.updated} updated, ${job.failed} failed`;
}

function JobSummary({ job }: { job: SyncJob }) {
  if (job.status === 'RUNNING') {
    const percent = job.total === 0 ? 0 : Math.round((job.processed / job.total) * 100);
    return (
      <Box>
        <Typography gutterBottom>
          Syncing #{job.fromId}–#{job.toId}: {job.processed} of {job.total} processed
        </Typography>
        <LinearProgress variant="determinate" value={percent} aria-label="Sync progress" />
        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
          {counters(job)}
        </Typography>
      </Box>
    );
  }
  const severity = job.status === 'FAILED' ? 'error' : job.failed > 0 ? 'warning' : 'success';
  return (
    <Alert severity={severity}>
      <Typography variant="body2">
        Last sync (#{job.fromId}–#{job.toId}) {job.status === 'FAILED' ? 'failed' : 'finished'}:{' '}
        {counters(job)}
      </Typography>
      {job.errors.length > 0 && (
        <Box component="ul" sx={{ m: 0, pl: 2 }}>
          {job.errors.map((message) => (
            <li key={message}>
              <Typography variant="caption">{message}</Typography>
            </li>
          ))}
        </Box>
      )}
    </Alert>
  );
}

export function SyncPokemonDialog({
  open,
  job,
  starting,
  error,
  onStart,
  onClose,
}: SyncPokemonDialogProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<SyncFormInput, unknown, SyncFormValues>({
    resolver: zodResolver(syncFormSchema),
    defaultValues: DEFAULT_SYNC_RANGE,
  });
  const running = job?.status === 'RUNNING';

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm" aria-labelledby="sync-title">
      <form onSubmit={handleSubmit((values) => onStart(values))} noValidate>
        <DialogTitle id="sync-title">Sync Pokémon data</DialogTitle>
        <DialogContent dividers>
          <DialogContentText sx={{ mb: 2 }}>
            Imports the PokeAPI range into the local database. Pokémon already stored get their
            catalog data refreshed; local name, region and tags are kept.
          </DialogContentText>
          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}
          <Grid container spacing={2} sx={{ mb: job ? 2 : 0 }}>
            {(['fromId', 'toId'] as const).map((field) => (
              <Grid key={field} size={6}>
                <TextField
                  fullWidth
                  type="number"
                  label={field === 'fromId' ? 'From #' : 'To #'}
                  disabled={running}
                  slotProps={{ htmlInput: { min: 1, max: MAX_POKEAPI_ID, step: 1 } }}
                  {...register(field)}
                  error={Boolean(errors[field])}
                  helperText={errors[field]?.message}
                />
              </Grid>
            ))}
          </Grid>
          {job && <JobSummary job={job} />}
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose}>Close</Button>
          <Button type="submit" variant="contained" disabled={running || starting}>
            Start sync
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}
