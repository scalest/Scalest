import React from 'react';
import {
  Route,
  Redirect,
} from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function PrivateRoute({ children, ...rest }) {
  const { isAuthed } = useAuth();
  return (
    <Route
      {...rest}
      render={({ location }) => (isAuthed() ? (children) : (
        <Redirect
          to={{
            pathname: '/login',
            state: { from: location },
          }}
        />
      ))}
    />
  );
}

export default PrivateRoute;
