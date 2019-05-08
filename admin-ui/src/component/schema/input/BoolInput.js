import Switch from '@material-ui/core/Switch';
import React from "react"

function BoolInput({ field, item, setItem }) {
  return <Switch checked={item} onChange={e => setItem(e.target.checked)}></Switch>
}

export default BoolInput
