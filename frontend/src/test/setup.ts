import '@testing-library/jest-dom/vitest';
import { cleanup } from '@testing-library/react';
import { afterAll, afterEach, beforeAll, vi } from 'vitest';
import { server } from './server';

const consoleProblems: string[] = [];

beforeAll(() => {
  server.listen({ onUnhandledRequest: 'error' });
  for (const level of ['error', 'warn'] as const) {
    vi.spyOn(console, level).mockImplementation((...args: unknown[]) => {
      consoleProblems.push(`console.${level}: ${args.map(String).join(' ')}`);
    });
  }
});

afterEach(() => {
  cleanup();
  server.resetHandlers();
  localStorage.clear();
  const problems = consoleProblems.splice(0);
  if (problems.length > 0) {
    throw new Error(`Tests must not log warnings or errors:\n${problems.join('\n')}`);
  }
});

afterAll(() => {
  server.close();
  vi.restoreAllMocks();
});
