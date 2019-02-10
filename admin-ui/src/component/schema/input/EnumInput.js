import React from 'react';
import { MenuItem, Select, FormControl } from '@material-ui/core';

function EnumInput({ field, item, setItem }) {
  return (
    <FormControl variant="outlined">
      <Select value={item} onChange={(e) => setItem(e.target.value)}>
        {
      field.schema.addition.values.map((v) => <MenuItem key={v} value={v}>{v}</MenuItem>)
    }
      </Select>
    </FormControl>
  );
}

export default EnumInput;
