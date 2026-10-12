import { apiClient } from '../../shared/api/client';
import type { TokenResponse, UserResponse } from '../../shared/api/types';

export interface Credentials {
  username: string;
  password: string;
}

export async function login(credentials: Credentials): Promise<TokenResponse> {
  const { data } = await apiClient.post<TokenResponse>('/auth/login', credentials);
  return data;
}

export async function register(credentials: Credentials): Promise<UserResponse> {
  const { data } = await apiClient.post<UserResponse>('/auth/register', credentials);
  return data;
}
