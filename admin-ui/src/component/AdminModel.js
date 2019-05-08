import React, { useEffect, useState, useRef } from 'react';
import FieldOutput from "./schema/output/FieldOutput"
import FieldInput from "./schema/input/FieldInput"
import { useAuth } from "./../context/AuthContext"
import { useParams } from "react-router-dom";
import MaterialTable from "material-table";
import tableIcons from '../util/TableIcons';

function AdminModel() {
    const tableRef = useRef();
    const { name } = useParams();
    const { client } = useAuth();
    const [info, setInfo] = useState();
    const [headers, setHeaders] = useState();

    async function fetchData() {
        const infoData = (await client.get('/admin/info', { logSuccess: true, logError: true })).data;
        const modelInfo = infoData.find(i => i.schema.name === name);
        setInfo(modelInfo);
        setHeaders(modelInfo.schema.fields.map(f => {
            return {
                title: f.name,
                field: f.name,
                initialEditValue: f.schema.default,
                emptyValue: f.schema.default,
                editable: f.name === 'id' ? 'never' : 'always',
                render: rowData => <FieldOutput field={f} item={rowData[f.name]} />,
                editComponent: props => {
                    const item = props.value;
                    if (item === undefined) props.onChange(f.schema.default);
                    return <FieldInput field={f} item={item} setItem={props.onChange} />
                },
            }
        }));
        if (tableRef.current) tableRef.current.onQueryChange();
    }

    useEffect(() => { fetchData(); }, [name])

    return (
        <MaterialTable
            tableRef={tableRef}
            icons={tableIcons}
            title={name}
            columns={headers}
            options={{
                search: false
            }}
            data={query =>
                client
                    .post(`/admin/api/${name}/search?page=${query.page}&size=${query.pageSize}`, { data: { id: null } })
                    .then(r => r.data)
                    .then(data => {
                        return {
                            data: data.content,
                            page: data.number,
                            totalCount: data.totalElements,
                        }
                    })
            }
            editable={{
                onRowAdd: newData => client.post(`/admin/api/${name}`, { data: newData, logSuccess: true, logError: true }),
                onRowUpdate: (newData) => client.put(`/admin/api/${name}`, { data: newData, logSuccess: true, logError: true }),
                onRowDelete: oldData => client.delete(`/admin/api/${name}`, { data: [oldData.id], logSuccess: true, logError: true })
            }}
        />
    )
}

export default AdminModel