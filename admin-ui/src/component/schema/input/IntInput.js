import React from "react"
import TextField from '@material-ui/core/TextField';
import NumberFormat from 'react-number-format';

function IntInput({ field, item, setItem }) {
  return <NumberFormat
    value={item}
    customInput={TextField}
    onChange={e => {
      const value = e.target.value;
      setItem(value === "" ? 0 : parseInt(value))
    }}
  />
}

export default IntInput

