package scalest.admin

import scalatags.Text.all._
import scalest.admin.Vue._

trait ModelAdminTemplate {

  def generateHtml(ma: ModelAdmin[_]): String = {
    html(
      head(headImports()),
      body(
        raw(ma.template),
        libsImports(),
        script(adminVueScript(ma.modelName, ma.modelViewRepr))
      )
    ).render
  }

  def generateTemplate(modelName: String, modelViewRepr: List[(String, ModelView)]): String =
    div(id := s"${modelName}App")(
      vApp(
        vContent(
          vContainer(
            vCard(
              vSnackbar(vModel := "notification")("""{{notificationText}}"""),
              vToolbar(attr("color") := "white", attr("flat"))(
                tag("v-toolbar-title")(
                  s"${modelName}s".capitalize
                ),
                tag("v-divider")(`class` := "mx-2", attr("inset"), attr("vertical")),
                vSpacer(),
                tag("v-dialog")(vModel := "dialog", maxWidth := "500px")(
                  vBtn(
                    attr("slot") := "activator",
                    attr("color") := "primary",
                    attr("dark"),
                    `class` := "mb-2"
                  )(
                    "New Item"
                  ),
                  vCard(
                    tag("v-card-title")(
                      span(`class` := "headline")("""{{ formTitle }}""")
                    ),
                    tag("v-card-text")(
                      vContainer(attr("grid-list-md"))(
                        tag("v-layout")(attr("wrap"))(
                          tag("v-flex")(attr("xs12"), attr("sm6"), attr("md4"))(
                            for ((n, mf) <- modelViewRepr) yield raw(mf.toInput(n))
                          )
                        )
                      )
                    ),
                    tag("v-card-actions")(
                      vSpacer,
                      vBtn(attr("color") := "blue darken-1", attr("flat"), `@click` := "close")("Cancel"),
                      vBtn(attr("color") := "blue darken-1", attr("flat"), `@click` := "save")("Save")
                    )
                  )
                )
              ),
              tag("v-data-table")(
                vBind("headers") := "headers",
                vBind("items") := "models",
                `class` := "elevation-1"
              )(
                tag("template")(attr("slot") := "items", attr("slot-scope") := "props")(
                  for ((n, mf) <- modelViewRepr) yield raw(mf.toOutput(n)),
                  td(`class` := "layout")(
                    vIcon(attr("small"), `class` := "mr-2", `@click` := "editItem(props.item)")("edit"),
                    vIcon(attr("small"), `@click` := "deleteItem(props.item)")("delete")
                  )
                )
              )
            )
          )
        )
      )
    ).render

  private def libsImports() = {
    Seq(
      script(src := "https://cdn.jsdelivr.net/npm/vue/dist/vue.js"),
      script(src := "https://cdnjs.cloudflare.com/ajax/libs/axios/0.18.0/axios.min.js"),
      script(src := "https://cdn.jsdelivr.net/npm/vuetify/dist/vuetify.js"),
      script(src := "https://cdn.jsdelivr.net/npm/vuelidate@0.7.4/dist/vuelidate.min.js")
    )
  }

  private def headImports() = {
    Seq(
      link(
        href := "https://fonts.googleapis.com/css?family=Roboto:100,300,400,500,700,900|Material+Icons",
        rel := "stylesheet"
      )
      ,
      link(
        href := "https://cdn.jsdelivr.net/npm/vuetify/dist/vuetify.min.css",
        rel := "stylesheet"
      )
      ,
      meta(
        name := "viewport",
        content := "width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no, minimal-ui"
      )
    )
  }


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

object ModelAdminTemplate
  extends ModelAdminTemplate