import { Alert, Box, Button, CircularProgress } from '@mui/material';
import { errorMessage } from '../api/errors';

export function Loading({ label = 'Loading' }: { label?: string }) {
  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}>
      <CircularProgress aria-label={label} />
    </Box>
  );
}

export function ErrorState({ error, onRetry }: { error: unknown; onRetry?: () => void }) {
  return (
    <Alert
      severity="error"
      action={
        onRetry && (
          <Button color="inherit" size="small" onClick={onRetry}>
            Retry
          </Button>
        )
      }
    >
      {errorMessage(error)}
    </Alert>
  );
}
