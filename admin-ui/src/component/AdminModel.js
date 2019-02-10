import React, { useEffect, useState, useRef } from 'react';
import { useParams } from 'react-router-dom';
import MaterialTable, { MTableToolbar } from 'material-table';
import { DeleteOutline } from '@material-ui/icons';
import { Chip, Paper, Grid } from '@material-ui/core';
import { CopyToClipboard } from 'react-copy-to-clipboard';
import FieldOutput from './schema/output/FieldOutput';
import fieldInput from './schema/input/FieldInput';
import { useAuth } from '../context/AuthContext';
import tableIcons from '../util/TableIcons';
import ModelForm from './ModelForm';

function AdminModel() {
  const tableRef = useRef();
  const { name } = useParams();
  const { client, hasModelPermission } = useAuth();
  const [info, setInfo] = useState();
  const [headers, setHeaders] = useState();
  const [actions, setActions] = useState([]);

  async function fetchData() {
    const protocolData = (await client.get('/admin/protocol', { logSuccess: true, logError: true })).data;
    const modelInfo = protocolData.find((i) => i.schema.name === name);
    setInfo(modelInfo);
    setHeaders(modelInfo.schema.fields.map((f) => ({
      title: f.name,
      field: f.name,
      initialEditValue: f.schema.default,
      emptyValue: f.schema.default,
      editable: f.name === 'id' ? 'never' : 'always',
      render: (rowData) => {
        const el = <FieldOutput field={f} item={rowData[f.name]} />;
        if (f.name === 'id') return <CopyToClipboard text={rowData[f.name]}>{el}</CopyToClipboard>;
        return el;
      },
      editComponent: ({ value, onChange }) => {
        const item = value;
        if (item === undefined) onChange(f.schema.default);
        return fieldInput({ field: f, item, setItem: onChange });
      },
    })));
    // const removeAction = {
    //   tooltip: 'Remove',
    //   icon: () => <DeleteOutline />,
    //   onClick: (evt, data) => client
    //     .delete(`/admin/api/${name}/delete`, { data: data.map((d) => d.id), logSuccess: true, logError: true })
    //     .then(() => tableRef.current.onQueryChange()),
    // };

    // const modelActions = Object.keys(modelInfo.actions);
    // modelActions.push(removeAction);
    // setActions(modelActions);

    if (tableRef.current) tableRef.current.onQueryChange();
  }

  useEffect(() => { fetchData(); }, [name]);

  const onRowAdd = (newData) => client.post(`/admin/api/${name}/create`, { data: newData, logSuccess: true, logError: true });
  const onRowUpdate = (newData) => client.put(`/admin/api/${name}/update`, { data: newData, logSuccess: true, logError: true });
  const onRowDelete = (oldData) => client.delete(`/admin/api/${name}/delete`, { data: [oldData.id], logSuccess: true, logError: true });
  const editable = {};
  if (hasModelPermission(name, 'UpdatePermission')) editable.onRowUpdate = onRowUpdate;
  if (hasModelPermission(name, 'CreatePermission')) editable.onRowAdd = onRowAdd;
  if (hasModelPermission(name, 'DeletePermission')) editable.onRowDelete = onRowDelete;
  const hasActions = () => info && info.actions.length !== 0;

  return (
    <Grid container spacing={3}>
      {hasActions() && (
        <Grid item xs={3}>
          <ModelForm info={info} tableRef={tableRef} />
        </Grid>
      )}
      <Grid item xs={hasActions() ? 9 : 12}>
        <MaterialTable
          tableRef={tableRef}
          icons={tableIcons}
          title={name}
          columns={headers}
          options={{
            search: false,
            selection: true,
            addRowPosition: 'first',
          }}
          actions={actions}
          data={(query) => client
            .post(`/admin/api/${name}/search?page=${query.page}&size=${query.pageSize}`, { data: { id: null } })
            .then((r) => r.data)
            .then((data) => ({
              data: data.content,
              page: data.number,
              totalCount: data.totalElements,
            }))}
          editable={editable}
        />
      </Grid>
    </Grid>
  );
}

export default AdminModel;
