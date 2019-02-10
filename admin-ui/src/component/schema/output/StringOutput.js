import React from 'react';
import { TextField } from '@material-ui/core';

function StringOutput({ field, item }) {
  return (
    <TextField
      disabled
      multiline
      id={field.name}
      label={field.name}
      variant="outlined"
      value={item}
    />
  );
}

export default StringOutput;
