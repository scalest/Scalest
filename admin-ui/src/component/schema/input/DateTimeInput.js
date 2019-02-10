import React from 'react';
import { DateTimePicker } from '@material-ui/pickers';

function DateTimeInput({ item, setItem }) {
  return (
    <DateTimePicker
      value={new Date(item)}
      onChange={(e) => setItem(e.toISOString().slice(0, -1))}
    />
  );
}

export default DateTimeInput;
