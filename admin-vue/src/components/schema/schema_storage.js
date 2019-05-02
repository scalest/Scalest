import Axios from 'axios'
import {ModelSchema} from "./model_schema";
import Vue from 'vue';

const SchemaStorage = {
  schemas: null,
  async all() {
    if (this.schemas === null) {
      try {
        this.schemas = Axios.get(`${process.env.VUE_APP_BACKEND_SERVICE_URL}/admin/schemas`)
          .then(res => res.data.map(s => new ModelSchema(s)));

        const components = (await this.schemas).flatMap(s => s.components());
        components.forEach(c => {
          Vue.component(c.id, eval(c.body))
        });
      } catch (e) {
        this.schemas = Promise.resolve([]);
      }
    }

    return await this.schemas;
  }
};

export default SchemaStorage