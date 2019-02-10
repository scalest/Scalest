import React, { useState } from 'react';
import HttpClient from '../util/HttpClient';

const AuthContext = React.createContext();

function parseJwt(token) {
  const base64Url = token.split('.')[1];
  const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
  const jsonPayload = decodeURIComponent(atob(base64).split('').map((c) => `%${(`00${c.charCodeAt(0).toString(16)}`).slice(-2)}`).join(''));

  return JSON.parse(jsonPayload);
}

function AuthProvider(props) {
  const [token, setToken] = useState(null); // localStorage.getItem('token')
  const isAuthed = () => token != null;
  const [user, setUser] = useState(isAuthed() ? parseJwt(token) : null);
  const [client, setClient] = useState(new HttpClient(isAuthed() ? { Authorization: `Bearer ${token}` } : {}));
  function hasPermission(permission) {
    if (!user) return false;
    if (user.isSuperUser) return true;
    return user.permissions.find((p) => p.$type === permission);
  }
  function hasModelPermission(model, permission) {
    if (!user) return false;
    if (user.isSuperUser) return true;
    return user.permissions.find((p) => p.$type === permission && p.model === model);
  }

  async function login(username, password) {
    const res = await client.post('/admin/login', { data: { username, password } });

    if (res.isOk) {
      const jwtToken = res.data.token;
      localStorage.setItem('token', jwtToken);
      setToken(jwtToken);
      setUser(parseJwt(jwtToken));
      setClient(new HttpClient({ Authorization: `Bearer ${jwtToken}` }));
    }

    return res;
  }

  const logout = () => {
    localStorage.removeItem('token');
    setUser(null);
    setToken(null);
    setClient(new HttpClient({}));
  };

  return (
    <AuthContext.Provider
      value={{
        token, client, user, hasPermission, hasModelPermission, login, logout, isAuthed,
      }}
      {...props}
    />
  );
}

function useAuth() {
  const context = React.useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within a AuthProvider');
  }
  return context;
}

export { AuthProvider, useAuth };
