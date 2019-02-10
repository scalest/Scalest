/* eslint-disable import/no-cycle */
import React, { useState } from 'react';
import { FixedSizeList } from 'react-window';
import {
  Button, ListItem, Grid, IconButton, Divider, Box,
} from '@material-ui/core';
import { Edit, Delete } from '@material-ui/icons';
import ModalButton from '../../../util/ModalButton';
import FieldInput from './FieldInput';

function ListInput({ field, item, setItem }) {
  const { elementSchema } = field.schema.addition;
  const [newItem, setNewItem] = useState(elementSchema.default);
  const Item = ({ index, style }) => (
    <div style={style}>
      <ListItem key={index}>
        <Grid container alignItems="center" justify="space-between">
          <Grid item>
            <FieldInput
              field={{ schema: elementSchema }}
              item={item[index]}
              setItem={(i) => {
                const list = [...item];
                list[index] = i;
                setItem(list);
              }}
            />
          </Grid>
          <Grid item>
            <IconButton
              variant="contained"
              onClick={() => {
                const list = [...item];
                list.splice(index, 1);
                setItem(list);
              }}
            >
              <Delete />
            </IconButton>
          </Grid>
        </Grid>
      </ListItem>
      <Divider />
    </div>
  );

  return (
    <ModalButton icon={Edit}>
      <Grid container justify="space-between" alignItems="center">
        <Grid item>
          <Box m={2}>
            <FieldInput
              field={{ schema: elementSchema }}
              item={newItem}
              setItem={setNewItem}
            />
          </Box>
        </Grid>
        <Grid item>
          <Box m={2}>
            <Button
              variant="contained"
              color="primary"
              onClick={() => {
                setItem([...item, newItem]);
                setNewItem(elementSchema.default);
              }}
            >
              Create
            </Button>
          </Box>
        </Grid>
      </Grid>
      <FixedSizeList height={400} width={300} itemSize={70} itemCount={item.length}>
        {Item}
      </FixedSizeList>
    </ModalButton>
  );
}

export default ListInput;
