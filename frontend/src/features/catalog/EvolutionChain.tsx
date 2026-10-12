import { Avatar, Box, Chip, Stack, Typography } from '@mui/material';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import { Link as RouterLink } from 'react-router-dom';
import type { EvolutionStage } from '../../shared/api/types';
import { displayName } from '../../shared/format';

function groupByStage(stages: EvolutionStage[]): EvolutionStage[][] {
  const groups = new Map<number, EvolutionStage[]>();
  stages.forEach((stage) => groups.set(stage.stage, [...(groups.get(stage.stage) ?? []), stage]));
  return [...groups.entries()].sort(([a], [b]) => a - b).map(([, group]) => group);
}

export function EvolutionChain({
  stages,
  currentName,
}: {
  stages: EvolutionStage[];
  currentName: string;
}) {
  if (stages.length <= 1) {
    return <Typography color="text.secondary">This Pokémon does not evolve.</Typography>;
  }
  const groups = groupByStage(stages);

  return (
    <Stack
      direction={{ xs: 'column', sm: 'row' }}
      spacing={2}
      alignItems="center"
      useFlexGap
      flexWrap="wrap"
      aria-label="Evolution chain"
    >
      {groups.map((group, index) => (
        <Stack
          key={group[0].stage}
          direction={{ xs: 'column', sm: 'row' }}
          spacing={2}
          alignItems="center"
        >
          {index > 0 && <ArrowForwardIcon color="action" aria-hidden />}
          <Stack direction="row" spacing={1} useFlexGap flexWrap="wrap" justifyContent="center">
            {group.map((stage) => (
              <Box key={stage.speciesId} sx={{ textAlign: 'center' }}>
                <Chip
                  component={RouterLink}
                  to={`/pokemon/${stage.name}`}
                  clickable
                  variant={stage.name === currentName ? 'filled' : 'outlined'}
                  color={stage.name === currentName ? 'primary' : 'default'}
                  avatar={
                    <Avatar src={stage.spriteUrl ?? undefined} alt={displayName(stage.name)} />
                  }
                  label={displayName(stage.name)}
                />
              </Box>
            ))}
          </Stack>
        </Stack>
      ))}
    </Stack>
  );
}
