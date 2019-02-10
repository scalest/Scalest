import React, { useState } from 'react';
import {
  Modal, DialogContent, Button, makeStyles, Dialog, IconButton,
} from '@material-ui/core';


const useStyles = makeStyles((theme) => ({
  modal: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
  paper: {
    backgroundColor: theme.palette.background.paper,
  },
}));

function ModalButton({ icon, children }) {
  const classes = useStyles();
  const [open, setOpen] = useState(false);
  const Icon = icon;
  return (
    <>
      <IconButton onClick={() => setOpen(true)} type="button" variant="contained" color="primary">
        <Icon />
      </IconButton>
      <Dialog
        className={classes.modal}
        open={open}
        onClose={() => setOpen(false)}
        aria-labelledby="simple-modal-title"
        aria-describedby="simple-modal-description"
      >
        <div className={classes.paper}>
          {children}
        </div>
      </Dialog>
    </>
  );
}

export default ModalButton;
