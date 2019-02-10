import React, { useState } from 'react';
import {
  Card, FormControl, InputLabel, Select, MenuItem, Box, Button, TextField, Grid,
} from '@material-ui/core';
import { useAuth } from '../context/AuthContext';

import fieldInput from './schema/input/FieldInput';

function defaultForSchema(schema) {
  const emptyForm = {};
  schema.fields.forEach((f) => {
    emptyForm[f.name] = f.schema.default;
  });
  return emptyForm;
}

function ActionsSelect({
  info, action, setForm, setAction,
}) {
  function setActionFromEvent(event) {
    const selectedAction = info.actions.find((a) => a.name === event.target.value);
    setForm(defaultForSchema(selectedAction));
    setAction(selectedAction);
  }

  return (
    <FormControl variant="outlined">
      <InputLabel id="actions">Actions</InputLabel>
      <Select
        labelId="actions"
        id="actions-select"
        value={action.name}
        onChange={setActionFromEvent}
      >
        {
            info.actions.map((a) => <MenuItem key={a.name} value={a.name}>{a.name}</MenuItem>)
          }
      </Select>
    </FormControl>
  );
}

function ModelForm({ info, tableRef }) {
  const [action, setAction] = useState(info.actions[0]);
  const [form, setForm] = useState(defaultForSchema(info.actions[0]));
  const { client } = useAuth();

  async function handleSubmit(event) {
    event.preventDefault();
    const settings = { data: { name: action.name, data: form }, logSuccess: true, logError: true };
    await client.put(`/admin/api/${info.schema.name}/action`, settings);
    if (tableRef.current) tableRef.current.onQueryChange();
  }

  if (!action) return <div>Loading</div>;

  return (
    <Card>
      <Box m={2}>
        <form onSubmit={handleSubmit}>
          <Grid
            container
            direction="column"
            justify="center"
            alignItems="center"
            spacing={2}
          >
            <Grid item>
              <ActionsSelect info={info} action={action} setAction={setAction} setForm={setForm} />
            </Grid>
            { action.fields.map((f) => (
              fieldInput({
                field: f,
                item: form[f.name],
                setItem: (e) => {
                  const updatedForm = { ...form };
                  updatedForm[f.name] = e;
                  setForm(updatedForm);
                },
              })
            )).map((i) => <Grid item>{i}</Grid>) }
            <Grid item>
              <Button type="submit" variant="contained" color="primary"> Submit </Button>
            </Grid>
          </Grid>
        </form>
      </Box>
    </Card>
  );
}

export default ModelForm;
