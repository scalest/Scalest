package scalest.admin

import scalatags.Text.all._

trait ModelAdminScript {
  type Script = String

  def generateScript(ma: ModelAdmin[_]): Script = script(adminVueScript(ma.modelName, ma.modelViewRepr)).render

  //Todo: Entity Validation using annotations
  private def adminVueScript(modelName: String, modelViewRepr: List[(String, ModelView)]) = {
    def renderHeaders() = (for ((n, _) <- modelViewRepr) yield s"""{ text: "${n.capitalize}", value: "$n" }""")
      .mkString("", ",", ",")

    def renderDefaults() = (for ((n, mv) <- modelViewRepr) yield s"""$n: ${mv.defaultValue()}""")
      .mkString(",")

    def renderFormParse() = (for ((n, mv) <- modelViewRepr) yield s"""${mv.parseForm(n)}""")
      .mkString(",")

    raw(
      //       language=JavaScript
      s"""
        Vue.config.devtools = true;

        var SnackbarNotificationQueue = {
          data: {
            notificationText: "",
            notificationQueue: [],
            notification: false
          },
          computed: {
            hasNotificationsPending() {
              return this.notificationQueue.length > 0;
            }
          },
          watch: {
            notification() {
              if (!this.notification && this.hasNotificationsPending) {
                this.notificationText = this.notificationQueue.shift();
                this.$$nextTick(() => (this.notification = true));
              }
            }
          },
          methods: {
            addNotification(text) {
              this.notificationQueue.push(text);
              if (!this.notification) {
                this.notificationText = this.notificationQueue.shift();
                this.notification = true;
              }
            }
          }
        };

        new Vue({
          mixins: [SnackbarNotificationQueue],
          el: "#${modelName}app",
          data() {
            return {
              dialog: false,
              headers: [
                // Generated
                ${renderHeaders()}
                { text: "Actions", value: "id", sortable: false }
              ],
              models: [],
              // autoKey: true,
              editedIndex: -1,
              // Both generated
              editedItem: {${renderDefaults()}},
              defaultItem: {
                id: 0,
                name: ""
              }
            };
          },
          watch: {
            dialog(val) {
              val || this.close();
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
          // Validations should be generated too
          validations() {
            return {};
          },
          methods: {
            isCreateMode() {
              return this.editedIndex === -1;
            },
            initialize() {
              axios
                .get("http://localhost:9000/api/${modelName}s")
                .then(r => (this.models = r.data));
            },
            editItem(item) {
              this.editedIndex = this.models.indexOf(item);
              this.editedItem = Object.assign({}, item);
              this.dialog = true;
            },

            deleteItem(item) {
              const index = this.models.indexOf(item);
              if (confirm("Are you sure you want to delete this item?")) {
                axios
                  .delete(`/api/${modelName}s/$${item.id}`)
                  .then(r => {
                    this.models.splice(index, 1);
                    this.addNotification("Successfully deleted item");
                  })
                  .catch(error => this.addNotification("Can`t delete item"));
              }
            },

            close() {
              this.dialog = false;
              setTimeout(() => {
                this.editedItem = Object.assign({}, this.defaultItem);
                this.editedIndex = -1;
              }, 300);
            },

            save() {
              const json = {${renderFormParse()}}

              if (this.editedIndex > -1) {
                axios
                  .put("/api/${modelName}s", json)
                  .then(r => {
                    Object.assign(this.models[this.editedIndex], json);
                    this.addNotification("Successfully updated item");
                  })
                  .catch(error => this.addNotification("Can`t update item"));
              } else {
                axios
                  .post("/api/${modelName}s", json)
                  .then(r => {
                    json.id = r.data;
                    this.models.push(json);
                    this.addNotification("Successfully created item");
                  })
                  .catch(error => this.addNotification("Can`t create item"));
              }
              this.close();
            }
          }
        });
        """
    )
  }
}

object ModelAdminScript
  extends ModelAdminScript
