import React from 'react';
import { Container, Box } from '@material-ui/core';
import { ToastContainer } from 'react-toastify';
import { BrowserRouter as Router, Switch, Route } from 'react-router-dom';
import { MuiPickersUtilsProvider } from '@material-ui/pickers';
import DateFnsUtils from '@date-io/date-fns';
import AdminToolbar from './component/toolbar/AdminToolbar';
import AdminHealth from './component/AdminHealth';
import AdminModel from './component/AdminModel';
import AdminLogin from './component/AdminLogin';
import AdminMain from './component/AdminMain';
import AdminDocumentation from './component/AdminDocumentation';
import PrivateRoute from './util/PrivateRoute';
import { AuthProvider } from './context/AuthContext';
import 'react-toastify/dist/ReactToastify.css';

function App() {
  return (
    <MuiPickersUtilsProvider utils={DateFnsUtils}>
      <Router>
        <AuthProvider>
          <AdminToolbar />
          <Container component={Box} mt={5}>
            <Switch>
              <PrivateRoute exact path={['/', '/admin']}>
                <AdminMain />
              </PrivateRoute>
              <Route path="/login">
                <AdminLogin />
              </Route>
              <PrivateRoute path="/model/:name">
                <AdminModel />
              </PrivateRoute>
              <PrivateRoute path="/health">
                <AdminHealth />
              </PrivateRoute>
              <PrivateRoute path="/documentation">
                <AdminDocumentation />
              </PrivateRoute>
            </Switch>
            <ToastContainer />
          </Container>
        </AuthProvider>
      </Router>
    </MuiPickersUtilsProvider>
  );
}

export default App;
