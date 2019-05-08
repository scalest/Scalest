import React from "react"
import TextField from '@material-ui/core/TextField';

function DoubleInput({ field, item, setItem }) {
    return <TextField type="number" value={item} onChange={e => setItem(parseFloat(e.target.value))}></TextField>
}

export default DoubleInput

