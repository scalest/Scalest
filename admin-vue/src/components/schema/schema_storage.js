import Axios from 'axios'
import {ModelSchema} from "./model_schema";
import Vue from 'vue';

const SchemaStorage = {
  schemas: null,
  async all() {
    if (this.schemas === null) {
      try {
        const res = await Axios.get(`${process.env.VUE_APP_BACKEND_SERVICE_URL}/admin/schemas`)
        this.schemas = res.data.map(s => new ModelSchema(s));
        const components = this.schemas.flatMap(s => s.components());
        components.forEach(c => {
          Vue.component(c.id, eval(c.body))
        });
      } catch (e) {
        this.schemas = [];
      }
    }

    return this.schemas;
  }
};

export default SchemaStorage