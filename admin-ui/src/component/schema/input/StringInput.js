import TextField from '@material-ui/core/TextField';
import React from 'react';

function StringInput({ field, item, setItem }) {
  return (
    <TextField
      multiline
      id={field.name}
      label={field.name}
      variant="outlined"
      value={item}
      onChange={(e) => setItem(e.target.value)}
    />
  );
}

export default StringInput;
