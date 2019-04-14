class ModelSchema {
    constructor(schema) {
        this.name = schema.name;
        this.fields = schema.fields;
    }

    headers() {
        const h = [];

        this.fields.forEach(f => {
            if (f.schema.outputType) {
                h.push({ text: f.name, value: f.name });
            }
        });

        h.push({ text: "Actions", value: "id", sortable: false });

        return h;
    }

    defaultItem() {
        const item = {};

        this.fields.forEach(f => (item[f.name] = f.schema.default));

        return item;
    }
}

export {
    ModelSchema
}