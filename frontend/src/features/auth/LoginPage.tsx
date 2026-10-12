import { Alert, Link, Typography } from '@mui/material';
import { Link as RouterLink, useLocation, useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { AuthForm } from './AuthForm';
import { useAuth } from './useAuth';

const loginSchema = z.object({
  username: z.string().trim().min(1, 'Username is required'),
  password: z.string().min(1, 'Password is required'),
});

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const redirectTo = (location.state as { from?: string } | null)?.from ?? '/collection';

  return (
    <AuthForm
      title="Sign in"
      submitLabel="Sign in"
      schema={loginSchema}
      onSubmit={async (values) => {
        await login(values);
        navigate(redirectTo, { replace: true });
      }}
      info={
        <Alert severity="info">
          Demo users: <strong>ash / pikachu123</strong> (user) and <strong>admin / admin123</strong>{' '}
          (admin)
        </Alert>
      }
      footer={
        <Typography variant="body2" textAlign="center">
          No account?{' '}
          <Link component={RouterLink} to="/register">
            Register
          </Link>
        </Typography>
      }
    />
  );
}
