/* eslint-disable import/no-cycle */
import React from 'react';
import { ListItem, Divider } from '@material-ui/core';
import { FixedSizeList } from 'react-window';
import { Visibility } from '@material-ui/icons';
import ModalButton from '../../../util/ModalButton';
import FieldOutput from './FieldOutput';

function ListOutput({ field, item }) {
  const Item = ({ index, style }) => (
    <div style={style}>
      <ListItem button key={index}>
        <FieldOutput
          key={index}
          field={{ schema: field.schema.addition.elementSchema }}
          item={item[index]}
        />
      </ListItem>
      <Divider />
    </div>
  );


  return (
    <ModalButton icon={Visibility}>
      <FixedSizeList height={400} width={300} itemSize={46} itemCount={item.length}>
        {Item}
      </FixedSizeList>
    </ModalButton>
  );
}

export default ListOutput;
