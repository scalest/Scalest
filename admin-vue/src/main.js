import Vue from 'vue'
import Vuetify from 'vuetify'
import Axios from "axios";
import VueCookie from 'vue-cookie'
import VueLocalStorage from 'vue-localstorage'
import App from './App.vue'
import router from './router'
import 'vuetify/dist/vuetify.min.css'
import SchemaStorage from './components/schema/schema_storage';

Vue.config.productionTip = false;

Axios.defaults.withCredentials = true;

Vue.prototype.$http = Axios;

Vue.use(VueCookie);
Vue.use(VueLocalStorage);
Vue.use(Vuetify);

const req = require.context('./components/schema/', true, /\.(vue)$/i);

req.keys().map(key => {
  const component = req(key).default;

  return Vue.component(component.name, component);
});

const authToken = Vue.cookie.get('SCALEST_ADMIN');

if (authToken) {
  Axios.defaults.headers.common['Authorization'] = `Basic ${authToken}`;
} else {
  delete Axios.defaults.headers.common['Authorization'];
}

new Vue({
  router,
  render: h => h(App)
}).$mount('#app');
