<template>
    <v-card flat="flat" tile="tile" class="mb-3">
        <v-toolbar dense>
            <v-toolbar-title>
                <v-btn flat to="/">
                    <span>Scalest Admin</span>
                </v-btn>
            </v-toolbar-title>
            <v-spacer></v-spacer>
            <v-menu offset-y="offset-y" v-if="models.length > 0">
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
                    >{{model}}
                    </v-list-tile>
                </v-list>
            </v-menu>
        </v-toolbar>
    </v-card>
</template>

<script>
  import SchemaStorage from './schema/schema_storage';

  export default {
    name: "scalest-header",
    data: function () {
      return {models: []};
    },
    async mounted() {
      if (this.$cookie.get("SCALEST_ADMIN")) {
        this.models = (await SchemaStorage.all()).map(s => s.name);
      }
    }
  };
</script>
