import Switch from '@material-ui/core/Switch';
import React from 'react';

function BoolInput({ item, setItem }) {
  return <Switch checked={item} onChange={(e) => setItem(e.target.checked)} />;
}

export default BoolInput;
