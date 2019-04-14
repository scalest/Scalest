<template>
  <v-card>
    <scalest-header/>
    <v-toolbar color="white" flat="flat">
      <v-toolbar-title>{{schema.name}}</v-toolbar-title>
      <v-divider class="mx-2" inset="inset" vertical="vertical"></v-divider>
      <v-spacer></v-spacer>
      <v-dialog v-model="dialog" style="max-width: 500px;">
        <v-btn slot="activator" color="primary" dark="dark" class="mb-2">New Item</v-btn>
        <v-card>
          <v-card-title>
            <span class="headline">{{ formTitle }}</span>
          </v-card-title>
          <v-card-text>
            <v-container grid-list-md="grid-list-md">
              <v-layout wrap="wrap">
                <v-flex xs12="xs12">
                  <template v-for="field in schema.fields">
                    <component
                      v-if="field.schema.inputType"
                      :key="field.name"
                      :is="field.schema.inputType"
                      :item="editedItem"
                      :field="field"
                    ></component>
                  </template>
                </v-flex>
              </v-layout>
            </v-container>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn color="blue darken-1" flat="flat" @click="close">Cancel</v-btn>
            <v-btn color="blue darken-1" flat="flat" @click="save">Save</v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
    </v-toolbar>
    <v-data-table v-bind:headers="headers" v-bind:items="models" class="elevation-1">
      <template slot="items" slot-scope="props">
        <template v-for="field in schema.fields">
          <td :key="field.name">
            <component
              v-if="field.schema.outputType"
              :key="field.name"
              :is="field.schema.outputType"
              :item="props.item"
              :field="field"
            />
          </td>
        </template>
        <td class="layout">
          <v-icon small="small" class="mr-2" @click="editItem(props.item)">edit</v-icon>
          <v-icon small="small" @click="deleteItem(props.item)">delete</v-icon>
        </td>
      </template>
    </v-data-table>
  </v-card>
</template>

<script>
import { ModelSchema } from "./schema/model_schema.js";
import ScalestHeader from "./ScalestHeader.vue";

export default {
  props: { name: String },
  name: "Model",
  components: {
    ScalestHeader
  },
  data() {
    return {
      dialog: false,
      headers: [],
      models: [],
      editedIndex: -1,
      schema: {},
      editedItem: {}
    };
  },
  watch: {
    dialog(val) {
      val || this.close();
    },
    $route(to, from) {
      this.initialize();
    }
  },
  created() {
    this.initialize();
  },
  computed: {
    formTitle() {
      return this.isCreateMode() ? "New Item" : "Edit Item";
    }
  },
  validations() {
    return {};
  },
  methods: {
    isCreateMode() {
      return this.editedIndex === -1;
    },
    initialize() {
      this.$http
        .get(`${process.env.VUE_APP_BACKEND_SERVICE_URL}/api/${this.name}`)
        .then(r => (this.models = r.data));

      const schemas = JSON.parse(this.$localStorage.get("schemas", "[]")).map(
        s => new ModelSchema(s)
      );

      // console.log("Model got schema from localStorage", schemas);

      this.schema = schemas.find(s => s.name === this.name);
      this.editedItem = this.schema.defaultItem();
      this.headers = this.schema.headers();
    },
    editItem(item) {
      this.editedIndex = this.models.indexOf(item);
      this.editedItem = JSON.parse(JSON.stringify(item));
      this.dialog = true;
    },
    deleteItem(item) {
      const index = this.models.indexOf(item);
      if (confirm("Are you sure you want to delete this item?")) {
        this.$http
          .delete(
            `${process.env.VUE_APP_BACKEND_SERVICE_URL}/api/${this.name}`,
            {
              data: [item.id]
            }
          )
          .then(r => {
            this.models.splice(index, 1);

            this.$emit("notification", "Successfully deleted item");
          })
          .catch(error => this.$emit("notification", "Can`t delete item"));
      }
    },
    close() {
      this.dialog = false;
      setTimeout(() => {
        this.editedItem = Object.assign({}, this.schema.defaultItem());
        this.editedIndex = -1;
      }, 300);
    },
    save() {
      if (this.editedIndex > -1) {
        this.$http
          .put(
            `${process.env.VUE_APP_BACKEND_SERVICE_URL}/api/${this.name}`,
            this.editedItem
          )
          .then(r => {
            Object.assign(this.models[this.editedIndex], this.editedItem);
            this.$emit("notification", "Successfully updated item");
          })
          .catch(error => this.$emit("notification", "Can`t update item"));
      } else {
        this.$http
          .post(
            `${process.env.VUE_APP_BACKEND_SERVICE_URL}/api/${this.name}`,
            this.editedItem
          )
          .then(r => {
            this.editedItem.id = r.data;
            this.models.push(this.editedItem);
            this.$emit("notification", "Successfully created item");
          })
          .catch(error => this.$emit("notification", "Can`t create item"));
      }
      this.close();
    }
  }
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
