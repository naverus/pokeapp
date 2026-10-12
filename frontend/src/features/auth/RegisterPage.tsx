import { Link, Typography } from '@mui/material';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { register } from './api';
import { AuthForm } from './AuthForm';
import { useAuth } from './useAuth';

const registerSchema = z.object({
  username: z
    .string()
    .trim()
    .min(3, 'At least 3 characters')
    .max(30, 'At most 30 characters')
    .regex(/^[A-Za-z0-9_.-]+$/, "Only letters, digits, '.', '_' or '-'"),
  password: z
    .string()
    .min(8, 'At least 8 characters')
    .max(72, 'At most 72 characters')
    .regex(/[A-Za-z]/, 'Must contain a letter')
    .regex(/\d/, 'Must contain a digit'),
});

export function RegisterPage() {
  const { login } = useAuth();
  const navigate = useNavigate();

  return (
    <AuthForm
      title="Create an account"
      submitLabel="Register"
      schema={registerSchema}
      onSubmit={async (values) => {
        await register(values);
        await login(values);
        navigate('/collection', { replace: true });
      }}
      footer={
        <Typography variant="body2" textAlign="center">
          Already registered?{' '}
          <Link component={RouterLink} to="/login">
            Sign in
          </Link>
        </Typography>
      }
    />
  );
}
