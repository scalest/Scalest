class ModelSchema {

  constructor(schema) {
    this.name = schema.name;
    this.fields = schema.fields;
  }

  headers() {
    const h = [];

    this.fields.forEach(f => {
      if (f.schema.outputType) {
        h.push({text: f.name, value: f.name});
      } else if (f.schema.outputComponent) {
        h.push({text: f.name, value: f.name});
      }
    });

    h.push({text: "Actions", value: "id", sortable: false});

    return h;
  }

  defaultItem() {
    const item = {};

    this.fields.forEach(f => (item[f.name] = f.schema.default));

    return item;
  }

  components() {
    return this.fields.flatMap(f => {
      const components = [];
      if (f.schema.outputComponent !== undefined) components.push(f.schema.outputComponent);
      if (f.schema.inputComponent !== undefined) components.push(f.schema.inputComponent);
      return components;
    });
  }
}

export {
  ModelSchema
}