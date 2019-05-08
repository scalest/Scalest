import TextField from '@material-ui/core/TextField';
import React from "react"

function StringInput({ field, item, setItem }) {
  return <TextField value={item} onChange={e => setItem(e.target.value)}></TextField>
}

export default StringInput
