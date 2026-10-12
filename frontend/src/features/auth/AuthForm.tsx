import { useState, type ReactNode } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Alert, Box, Button, Card, CardContent, Stack, TextField, Typography } from '@mui/material';
import { errorMessage, toProblem } from '../../shared/api/errors';
import type { Credentials } from './api';

interface AuthFormProps {
  title: string;
  submitLabel: string;
  schema: z.ZodType<Credentials>;
  onSubmit: (values: Credentials) => Promise<void>;
  footer?: ReactNode;
  info?: ReactNode;
}

export function AuthForm({ title, submitLabel, schema, onSubmit, footer, info }: AuthFormProps) {
  const [serverError, setServerError] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<Credentials>({
    resolver: zodResolver(schema),
    defaultValues: { username: '', password: '' },
  });

  const submit = handleSubmit(async (values) => {
    setServerError(null);
    try {
      await onSubmit(values);
    } catch (error) {
      const fieldErrors = toProblem(error).errors ?? {};
      Object.entries(fieldErrors).forEach(([field, message]) => {
        if (field === 'username' || field === 'password') setError(field, { message });
      });
      setServerError(errorMessage(error));
    }
  });

  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', mt: { xs: 2, sm: 6 } }}>
      <Card sx={{ width: '100%', maxWidth: 420 }}>
        <CardContent>
          <Stack component="form" spacing={2} onSubmit={submit} noValidate>
            <Typography variant="h5" component="h1">
              {title}
            </Typography>
            {info}
            {serverError && <Alert severity="error">{serverError}</Alert>}
            <TextField
              label="Username"
              autoComplete="username"
              {...register('username')}
              error={Boolean(errors.username)}
              helperText={errors.username?.message}
            />
            <TextField
              label="Password"
              type="password"
              autoComplete="current-password"
              {...register('password')}
              error={Boolean(errors.password)}
              helperText={errors.password?.message}
            />
            <Button type="submit" variant="contained" size="large" disabled={isSubmitting}>
              {submitLabel}
            </Button>
            {footer}
          </Stack>
        </CardContent>
      </Card>
    </Box>
  );
}
