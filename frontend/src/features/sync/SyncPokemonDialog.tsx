import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  Alert,
  Box,
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Grid,
  LinearProgress,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import type { SyncJob, SyncRangePayload } from '../../shared/api/types';
import {
  QUICK_PICKS,
  RESTOCK_TEXT,
  restockCounters,
  restockHeadline,
  restockProgress,
  restockSeverity,
} from './restockCopy';
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

function JobSummary({ job }: { job: SyncJob }) {
  if (job.status === 'RUNNING') {
    const percent = job.total === 0 ? 0 : Math.round((job.processed / job.total) * 100);
    return (
      <Box>
        <Typography gutterBottom>{restockProgress(job)}</Typography>
        <LinearProgress
          variant="determinate"
          value={percent}
          aria-label={RESTOCK_TEXT.progressLabel}
        />
        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
          {restockCounters(job)}
        </Typography>
      </Box>
    );
  }
  return (
    <Alert severity={restockSeverity(job)}>
      <Typography variant="body2">{restockHeadline(job)}</Typography>
      <Typography variant="caption" color="text.secondary">
        #{job.fromId}–#{job.toId}: {restockCounters(job)}
      </Typography>
      {job.errors.length > 0 && (
        <>
          <Typography variant="body2" sx={{ mt: 1 }}>
            {RESTOCK_TEXT.gotAway}
          </Typography>
          <Box component="ul" sx={{ m: 0, pl: 2 }}>
            {job.errors.map((message) => (
              <li key={message}>
                <Typography variant="caption">{message}</Typography>
              </li>
            ))}
          </Box>
        </>
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
    setValue,
    formState: { errors },
  } = useForm<SyncFormInput, unknown, SyncFormValues>({
    resolver: zodResolver(syncFormSchema),
    defaultValues: DEFAULT_SYNC_RANGE,
  });
  const running = job?.status === 'RUNNING';

  const pick = (fromId: number, toId: number) => {
    setValue('fromId', fromId, { shouldValidate: true });
    setValue('toId', toId, { shouldValidate: true });
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm" aria-labelledby="restock-title">
      <form onSubmit={handleSubmit((values) => onStart(values))} noValidate>
        <DialogTitle id="restock-title">{RESTOCK_TEXT.title}</DialogTitle>
        <DialogContent dividers>
          <DialogContentText sx={{ mb: 2 }}>{RESTOCK_TEXT.intro}</DialogContentText>
          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}
          <Stack
            direction="row"
            spacing={1}
            useFlexGap
            sx={{ flexWrap: 'wrap', mb: 2 }}
            role="group"
            aria-label={RESTOCK_TEXT.quickPicks}
          >
            {QUICK_PICKS.map((quickPick) => (
              <Chip
                key={quickPick.label}
                label={quickPick.label}
                variant="outlined"
                color="primary"
                disabled={running}
                onClick={() => pick(quickPick.fromId, quickPick.toId)}
              />
            ))}
          </Stack>
          <Grid container spacing={2} sx={{ mb: job ? 2 : 0 }}>
            {(['fromId', 'toId'] as const).map((field) => (
              <Grid key={field} size={6}>
                <TextField
                  fullWidth
                  type="number"
                  label={field === 'fromId' ? RESTOCK_TEXT.fromLabel : RESTOCK_TEXT.toLabel}
                  disabled={running}
                  slotProps={{
                    htmlInput: { min: 1, max: MAX_POKEAPI_ID, step: 1 },
                    inputLabel: { shrink: true },
                  }}
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
          <Button onClick={onClose}>{RESTOCK_TEXT.close}</Button>
          <Button type="submit" variant="contained" disabled={running || starting}>
            {RESTOCK_TEXT.start}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}
