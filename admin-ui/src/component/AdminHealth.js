import React, { useEffect, useState, Fragment } from 'react';
import { useAuth } from "./../context/AuthContext"
import { makeStyles } from '@material-ui/core/styles';
import { Box, Paper, Typography, Table, TableBody, TableCell, TableContainer, TableHead, TableRow } from '@material-ui/core';

const useStyles = makeStyles({
    table: {
        minWidth: 100,
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
    useEffect(() => { fetchData(); }, [])
    if (!health) return <span>Loading...</span>;

    return (
        <TableContainer component={Paper} elevation={2}>
            <Table className={classes.table} size="small" aria-label="a dense table">
                <TableHead>
                    <TableRow>
                        <TableCell>Name</TableCell>
                        <TableCell align="right">Status</TableCell>
                        <TableCell align="right">Description</TableCell>
                        <TableCell align="right">Addition</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {health.statuses.map(row => (
                        <TableRow key={row.name}>
                            <TableCell component="th" scope="row">{row.name}</TableCell>
                            <TableCell align="right">{row.status}</TableCell>
                            <TableCell align="right">{row.description}</TableCell>
                            <TableCell align="right">{row.addition}</TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </TableContainer>
    )
}

export default AdminHealth