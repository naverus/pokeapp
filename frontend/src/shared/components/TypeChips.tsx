import { Chip, Stack } from '@mui/material';
import { typeColors } from '../theme';

export function TypeChips({
  types,
  size = 'small',
}: {
  types: string[];
  size?: 'small' | 'medium';
}) {
  return (
    <Stack direction="row" spacing={0.5} useFlexGap flexWrap="wrap">
      {types.map((type) => (
        <Chip
          key={type}
          label={type}
          size={size}
          sx={{
            bgcolor: typeColors[type] ?? 'grey.500',
            color: '#fff',
            fontWeight: 600,
            textTransform: 'capitalize',
          }}
        />
      ))}
    </Stack>
  );
}
