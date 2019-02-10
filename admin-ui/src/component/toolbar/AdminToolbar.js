import React, { useEffect, useState } from 'react';
import {
  AppBar, Toolbar, Box, Button, IconButton,
} from '@material-ui/core';
import { Healing, Home, Description } from '@material-ui/icons';
import PersonAddIcon from '@material-ui/icons/PersonRounded';
import { Link as RouterLink, useLocation, useHistory } from 'react-router-dom';
import AdminMenu from './AdminMenu';
import { useAuth } from '../../context/AuthContext';
import Guest from '../../util/Guest';
import Private from '../../util/Private';

const Link = React.forwardRef((props, ref) => <RouterLink innerRef={ref} {...props} />);

function LoginButton() {
  const location = useLocation();

  if (location.pathname === '/login') return null;

  return (
    <Guest>
      <Button component={Link} to="/login" color="inherit">
        Login
        <PersonAddIcon />
      </Button>
    </Guest>
  );
}

function LogoutButton() {
  const { logout } = useAuth();
  const history = useHistory();

  function logoutUser() {
    logout();
    history.push('/');
  }

  return (
    <Button onClick={logoutUser} color="inherit">
      Logout
      <PersonAddIcon />
    </Button>
  );
}

function ModelMenu() {
  const [links, setLinks] = useState();
  const { client } = useAuth();

  async function fetchData() {
    const settings = { logSuccess: true, logError: true };
    const protocolsData = (await client.get('/admin/protocol', settings)).data;
    const linksFromData = protocolsData.map((protocol) => {
      const { name } = protocol.schema;
      return { name, to: `/model/${name}` };
    });
    setLinks(linksFromData);
  }

  useEffect(() => {
    fetchData();
  }, []);

  if (!links) return null;
  return <AdminMenu name="Models" links={links} />;
}

function HealthButton() {
  const { hasPermission } = useAuth();
  if (!hasPermission('HealthPermission')) return null;

  return (
    <Button component={Link} to="/health" edge="start" color="inherit">
      <Healing />
      Health
    </Button>
  );
}

function DocumentationButton() {
  const { hasPermission } = useAuth();
  if (!hasPermission('SwaggerPermission')) return null;

  return (
    <Button component={Link} to="/documentation" edge="start" color="inherit">
      <Description />
      Documentation
    </Button>
  );
}

function AdminToolbar() {
  return (
    <Private>
      <AppBar position="static">
        <Toolbar>
          <IconButton component={Link} to="/" edge="start" color="inherit">
            <Home />
          </IconButton>
          <Box flexGrow={1} />
          <HealthButton />
          <ModelMenu />
          <DocumentationButton />
          <LoginButton />
          <LogoutButton />
        </Toolbar>
      </AppBar>
    </Private>
  );
}

export default AdminToolbar;
