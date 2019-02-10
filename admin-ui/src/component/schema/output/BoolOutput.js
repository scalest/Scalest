import CheckCircleOutlineIcon from '@material-ui/icons/CheckCircleOutline';
import { makeStyles } from '@material-ui/core/styles';
import CancelIcon from '@material-ui/icons/Cancel';
import React from 'react';

const useStyles = makeStyles({
  successIcon: {
    color: 'green',
  },
});

function BoolOutput({ item }) {
  const classes = useStyles();

  if (item) {
    return <CheckCircleOutlineIcon color="action" className={classes.successIcon} />;
  }

  return <CancelIcon color="error" />;
}

export default BoolOutput;
