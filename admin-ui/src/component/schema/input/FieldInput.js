/* eslint-disable import/no-cycle */
import React from 'react';
import BoolInput from './BoolInput';
import StringInput from './StringInput';
import IntInput from './IntInput';
import DoubleInput from './DoubleInput';
import EnumInput from './EnumInput';
import JsonInput from './JsonInput';
import ListInput from './ListInput';
import DateTimeInput from './DateTimeInput';

const inputs = {
  'bool-input': BoolInput,
  'string-input': StringInput,
  'int-input': IntInput,
  'enum-input': EnumInput,
  'double-input': DoubleInput,
  'json-input': JsonInput,
  'list-input': ListInput,
  'date-time-input': DateTimeInput,
};

function DefaultInput() {
  return <span>No input for such type</span>;
}

function fieldInput({ field, item, setItem }) {
  const specificInput = inputs[field.schema.inputType] || DefaultInput;
  return specificInput({ field, item, setItem });
}

export default fieldInput;
