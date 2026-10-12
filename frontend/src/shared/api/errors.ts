import axios from 'axios';
import type { ProblemDetail } from './types';

export function toProblem(error: unknown): ProblemDetail {
  if (axios.isAxiosError<ProblemDetail>(error)) {
    if (error.response?.data && typeof error.response.data === 'object') {
      return { status: error.response.status, ...error.response.data };
    }
    if (error.response) {
      return { status: error.response.status, detail: error.message };
    }
    return { detail: 'The server could not be reached. Please try again.' };
  }
  return { detail: error instanceof Error ? error.message : 'Unexpected error' };
}

export function errorMessage(error: unknown): string {
  const problem = toProblem(error);
  return problem.detail ?? problem.title ?? 'Unexpected error';
}
