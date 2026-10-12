import { AppBar, Box, Button, Chip, Container, Stack, Toolbar, Typography } from '@mui/material';
import CatchingPokemonIcon from '@mui/icons-material/CatchingPokemon';
import { NavLink, Outlet, Link as RouterLink } from 'react-router-dom';
import { useAuth } from '../../features/auth/useAuth';

const navButtonSx = {
  color: 'inherit',
  '&.active': { bgcolor: 'rgba(255,255,255,0.18)' },
} as const;

export function AppLayout() {
  const { session, isAuthenticated, logout } = useAuth();

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <AppBar position="sticky" elevation={0}>
        <Toolbar sx={{ gap: 1, flexWrap: 'wrap' }}>
          <CatchingPokemonIcon aria-hidden />
          <Typography
            variant="h6"
            component={RouterLink}
            to="/"
            sx={{ color: 'inherit', textDecoration: 'none', fontWeight: 700, mr: 2 }}
          >
            PokeApp
          </Typography>
          <Stack direction="row" spacing={1} component="nav" aria-label="Main" sx={{ flexGrow: 1 }}>
            <Button component={NavLink} to="/" end sx={navButtonSx}>
              Browse
            </Button>
            <Button component={NavLink} to="/collection" sx={navButtonSx}>
              My collection
            </Button>
          </Stack>
          {isAuthenticated && session ? (
            <Stack direction="row" spacing={1} alignItems="center">
              <Chip
                label={`${session.username} · ${session.role}`}
                size="small"
                sx={{ bgcolor: 'rgba(255,255,255,0.2)', color: 'inherit' }}
              />
              <Button color="inherit" onClick={logout}>
                Sign out
              </Button>
            </Stack>
          ) : (
            <Button color="inherit" component={RouterLink} to="/login">
              Sign in
            </Button>
          )}
        </Toolbar>
      </AppBar>
      <Container maxWidth="lg" sx={{ py: { xs: 2, md: 4 } }}>
        <Outlet />
      </Container>
    </Box>
  );
}
