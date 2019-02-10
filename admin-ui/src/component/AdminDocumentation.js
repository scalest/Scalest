import React from 'react';
import { RedocStandalone } from 'redoc';

function AdminDocumentation() {
  const swaggerUrl = `${process.env.REACT_APP_API_URL}/api/swagger`;
  return (
    <RedocStandalone specUrl={swaggerUrl} />
  );
}

export default AdminDocumentation;
