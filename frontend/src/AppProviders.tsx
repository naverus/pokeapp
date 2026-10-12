import type { ReactNode } from 'react';
import { CssBaseline, ThemeProvider } from '@mui/material';
import { QueryClientProvider, type QueryClient } from '@tanstack/react-query';
import { AuthProvider } from './features/auth/AuthProvider';
import { theme } from './shared/theme';

export function AppProviders({ client, children }: { client: QueryClient; children: ReactNode }) {
  return (
    <QueryClientProvider client={client}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <AuthProvider>{children}</AuthProvider>
      </ThemeProvider>
    </QueryClientProvider>
  );
}
