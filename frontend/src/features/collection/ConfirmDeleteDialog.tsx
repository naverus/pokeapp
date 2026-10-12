import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
} from '@mui/material';

interface ConfirmDeleteDialogProps {
  name: string | null;
  pending: boolean;
  onCancel: () => void;
  onConfirm: () => void;
}

export function ConfirmDeleteDialog({
  name,
  pending,
  onCancel,
  onConfirm,
}: ConfirmDeleteDialogProps) {
  return (
    <Dialog open={name !== null} onClose={onCancel} aria-labelledby="confirm-delete-title">
      <DialogTitle id="confirm-delete-title">Delete Pokémon</DialogTitle>
      <DialogContent>
        <DialogContentText>
          Remove <strong>{name}</strong> from the local database? This cannot be undone.
        </DialogContentText>
      </DialogContent>
      <DialogActions>
        <Button onClick={onCancel}>Cancel</Button>
        <Button color="error" variant="contained" onClick={onConfirm} disabled={pending}>
          Delete
        </Button>
      </DialogActions>
    </Dialog>
  );
}
