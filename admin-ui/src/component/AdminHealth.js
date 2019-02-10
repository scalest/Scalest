import React, { useEffect, useState } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import {
  Box, Paper, Typography, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Grid,
} from '@material-ui/core';
import { useAuth } from '../context/AuthContext';
import EnumOutput from './schema/output/EnumOutput';

const useStyles = makeStyles({
  table: {
    minWidth: 100,
  },
  healthcheks: {
    font: 'bold 150px arial, sans-serif',
    textShadow: '2px 2px 3px rgba(255,255,255,0.5)',
    backgroundColor: '#565656',
    color: 'transparent',
    '-webkit-background-clip': 'text',
    '-moz-background-clip': 'text',
    'background-clip': 'text',
  },
});

function AdminHealth() {
  const { client } = useAuth();
  const classes = useStyles();
  const [health, setHealth] = useState();

  async function fetchData() {
    const settings = { logSuccess: true, logError: true };
    const response = await client.get('/health', settings);
    if (response.isOk) setHealth(response.data);
  }
  useEffect(() => { fetchData(); }, []);
  if (!health) return <span>Loading...</span>;

  return (
    <Grid container justify="center">
      <Grid container item xs={12} justify="center">
        <Box mb={5}>
          <Typography variant="h1" component="h2" className={classes.healthcheks}>
                        Health
          </Typography>
        </Box>
      </Grid>
      <Grid item xs={8}>
        <TableContainer component={Paper} elevation={2}>
          <Table className={classes.table} aria-label="simple table">
            <TableHead>
              <TableRow>
                <TableCell>Name</TableCell>
                <TableCell align="right">Status</TableCell>
                <TableCell align="right">Description</TableCell>
                <TableCell align="right">Addition</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {health.statuses.map((row) => (
                <TableRow key={row.name}>
                  <TableCell component="th" scope="row">{row.name}</TableCell>
                  <TableCell align="right">
                    <EnumOutput item={row.status} />
                  </TableCell>
                  <TableCell align="right">{row.description}</TableCell>
                  <TableCell align="right">{row.addition}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </Grid>
    </Grid>
  );
}

export default AdminHealth;
