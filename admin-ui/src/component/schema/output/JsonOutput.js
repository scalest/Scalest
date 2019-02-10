import React from 'react';
import JSONPretty from 'react-json-pretty';
import 'react-json-pretty/themes/monikai.css';
import { Visibility } from '@material-ui/icons';
import ModalButton from '../../../util/ModalButton';

function JsonOutput({ item }) {
  return (
    <ModalButton icon={Visibility}>
      <JSONPretty data={item} />
    </ModalButton>
  );
}

export default JsonOutput;
