import { Box, LinearProgress, Stack, Typography } from '@mui/material';
import type { Stat } from '../../shared/api/types';
import { displayName } from '../../shared/format';

const MAX_BASE_STAT = 255;

export function StatBars({ stats, total }: { stats: Stat[]; total: number }) {
  return (
    <Stack spacing={1.5}>
      {stats.map((stat) => (
        <Box key={stat.name}>
          <Stack direction="row" justifyContent="space-between">
            <Typography variant="body2">{displayName(stat.name)}</Typography>
            <Typography variant="body2" fontWeight={700}>
              {stat.baseValue}
            </Typography>
          </Stack>
          <LinearProgress
            variant="determinate"
            value={Math.min(100, (stat.baseValue / MAX_BASE_STAT) * 100)}
            aria-label={`${displayName(stat.name)} ${stat.baseValue}`}
            sx={{ height: 8, borderRadius: 4 }}
          />
        </Box>
      ))}
      <Typography variant="body2" textAlign="right">
        Total: <strong>{total}</strong>
      </Typography>
    </Stack>
  );
}
