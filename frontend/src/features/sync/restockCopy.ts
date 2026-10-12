import type { SyncJob } from '../../shared/api/types';

/** User-facing wording of the admin bulk sync, presented as "Restock Collection". */
export const RESTOCK_TEXT = {
  button: 'Restock Collection',
  title: 'Restock your collection',
  intro:
    "Pick a range of Pokédex numbers and we'll catch every Pokémon in it. Pokémon you already " +
    'have get fresh Pokédex info, and their local names, regions and tags stay exactly as you ' +
    'left them.',
  quickPicks: 'Quick picks',
  fromLabel: 'From Pokédex #',
  toLabel: 'To Pokédex #',
  start: 'Start restock',
  close: 'Close',
  progressLabel: 'Restock progress',
  gotAway: 'Got away:',
} as const;

export const QUICK_PICKS = [
  { label: 'Gen I (1–151)', fromId: 1, toId: 151 },
  { label: 'Gen II (152–251)', fromId: 152, toId: 251 },
  { label: 'Gen III (252–386)', fromId: 252, toId: 386 },
] as const;

export type RestockSeverity = 'success' | 'warning' | 'error';

export function restockSeverity(job: SyncJob): RestockSeverity {
  if (job.status === 'FAILED') return 'error';
  return job.failed > 0 ? 'warning' : 'success';
}

export function restockCounters(job: SyncJob): string {
  return `${job.inserted} caught · ${job.updated} refreshed · ${job.failed} got away`;
}

export function restockProgress(job: SyncJob): string {
  return `Restocking #${job.fromId}–#${job.toId}… ${job.processed} of ${job.total} checked`;
}

/** Headline of the finished-restock alert in the dialog. */
export function restockHeadline(job: SyncJob): string {
  switch (restockSeverity(job)) {
    case 'error':
      return 'The restock was interrupted.';
    case 'warning':
      return `Restock complete, but ${job.failed} Pokémon got away.`;
    default:
      return `Restock complete! ${job.inserted} caught, ${job.updated} refreshed.`;
  }
}

/** One-line snackbar message shown on the collection page when a restock finishes. */
export function restockNotice(job: SyncJob): { severity: RestockSeverity; message: string } {
  const severity = restockSeverity(job);
  const caught = `${job.inserted} caught, ${job.updated} refreshed`;
  switch (severity) {
    case 'error':
      return { severity, message: `Restock interrupted after catching ${job.inserted} Pokémon` };
    case 'warning':
      return { severity, message: `Restock complete: ${caught}, ${job.failed} got away` };
    default:
      return { severity, message: `Restock complete: ${caught}` };
  }
}
