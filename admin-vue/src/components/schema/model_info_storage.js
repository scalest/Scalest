import Axios from 'axios'
import {ModelInfo} from "./model_info";
import Vue from 'vue';

const ModelInfoStorage = {
  info: null,
  async all() {
    if (this.info === null) {
      try {
        this.info = Axios.get(`${process.env.VUE_APP_BACKEND_SERVICE_URL}/admin/info`)
          .then(res => res.data.map(info => new ModelInfo(info)));

        const components = (await this.info).flatMap(s => s.components());
        components.forEach(c => {
          Vue.component(c.id, eval(c.body))
        });
      } catch (e) {
        console.log(e);
        this.info = Promise.resolve([]);
      }
    }
    return await this.info;
  }
};

export default ModelInfoStorage