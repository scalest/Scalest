import React from 'react';
import { JsonEditor as Editor } from 'jsoneditor-react';
import 'jsoneditor-react/es/editor.min.css';
import { Edit } from '@material-ui/icons';
import ModalButton from '../../../util/ModalButton';

function JsonInput({ item, setItem }) {
  return (
    <ModalButton icon={Edit}>
      <Editor
        htmlElementProps={{
          style: { height: '500px', width: '500px' },
        }}
        value={item}
        onChange={setItem}
      />
    </ModalButton>
  );
}

export default JsonInput;
