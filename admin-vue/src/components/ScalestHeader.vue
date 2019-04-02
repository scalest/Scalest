<template>
  <v-card flat="flat" tile="tile" class="mb-3">
    <v-toolbar dense>
      <v-toolbar-title>
        <v-btn flat to="/">
          <span>Scalest Admin</span>
        </v-btn>
      </v-toolbar-title>
      <v-spacer></v-spacer>
      <v-menu offset-y="offset-y">
        <v-toolbar-title slot="activator">
          <span>Models</span>
          <v-icon>arrow_drop_down</v-icon>
        </v-toolbar-title>
        <v-list>
          <v-list-tile
            :key="model"
            v-for="model in models"
            :to="'/model/' + model"
            ripple="ripple"
          >{{model}}</v-list-tile>
        </v-list>
      </v-menu>
    </v-toolbar>
  </v-card>
</template>

<script>
import Vue from "vue";

export default {
  name: "scalest-header",
  data: function() {
    return { models: [] };
  },
  mounted() {
    if (this.$cookie.get("SCALEST_ADMIN")) {
      this.initSchemas();
    }
  },
  methods: {
    initSchemas() {
      this.$http
        .get(`${process.env.VUE_APP_BACKEND_SERVICE_URL}/admin/schemas`)
        .then(r => {
          const schemas = r.data;
          // console.log("Fetched schemas: ", schemas);
          Vue.localStorage.set("schemas", JSON.stringify(schemas));
          this.models = schemas.map(s => s.name);
        });
    }
  }
};
</script>
