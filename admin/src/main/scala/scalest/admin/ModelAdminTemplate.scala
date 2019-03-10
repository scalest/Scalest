package scalest.admin

import scalatags.Text.all._
import scalest.admin.Vue._

//Todo: rework whole stuff to some plain css and Vue, so i can drop vuetify,
trait ModelAdminTemplate {
  type Header = String
  type Template = String

  def generateSingleModelHtml(header: Header, ma: ModelAdmin[_]): String = {
    import ma.{modelView, script, template}

    html(
      head(headImports()),
      body(
        div(id := s"${modelView.modelName}App")(
          vApp(
            vContent(
              vContainer(
                raw(header),
                raw(template)
              )
            )
          )
        ),
        libsImports(),
        raw(script)
      )
    ).render
  }

  def generateHeader(mas: Seq[ModelAdmin[_]]): Header = {
    vCard(vColor := "grey lighten-4", attr("flat"), height := "100px", attr("tile"))(
      vToolbar(attr("dense"))(
        vToolbarSideIcon,
        vToolbarTitle("Scalest Admin"),
        vSpacer,
        vMenu(vBind("nudge-width") := "100")(
          vToolbarTitle(attr("slot") := "activator")(
            span("Models"),
            vIcon("arrow_drop_down")
          ),
          vList(
            for (ma <- mas) yield {
              vListTile(
                vListTileTitle(
                  a(href := s"/admin/${ma.modelView.modelName}")(ma.modelView.modelName.capitalize)
                )
              )
            }
          )
        )
      )

    ).render
  }

  def generateTemplate(ma: ModelAdmin[_]): Template = {
    import ma.modelView._

    vCard(
      vSnackbar(vModel := "notification")("""{{notificationText}}"""),
      vToolbar(vColor := "white", attr("flat"))(
        vToolbarTitle(
          s"${ma.modelView.modelName.replaceAll("_", " ")}s".capitalize
        ),
        vDivider(`class` := "mx-2", attr("inset"), attr("vertical")),
        vSpacer(),
        vDialog(vModel := "dialog", maxWidth := "500px")(
          vBtn(
            attr("slot") := "activator",
            vColor := "primary",
            attr("dark"),
            `class` := "mb-2"
          )(
            "New Item"
          ),
          vCard(
            vCardTitle(
              span(`class` := "headline")("""{{ formTitle }}""")
            ),
            vCardText(
              vContainer(attr("grid-list-md"))(
                vLayout(attr("wrap"))(
                  vFlex(attr("xs12"))(
                    modelRepr.map(_.toInput())
                  )
                )
              )
            ),
            vCardActions(
              vSpacer,
              vBtn(vColor := "blue darken-1", attr("flat"), `@click` := "close")("Cancel"),
              vBtn(vColor := "blue darken-1", attr("flat"), `@click` := "save")("Save")
            )
          )
        )
      ),
      vDataTable(
        vBind("headers") := "headers",
        vBind("items") := "models",
        `class` := "elevation-1"
      )(
        vTemplate(attr("slot") := "items", attr("slot-scope") := "props")(
          modelRepr.map(_.toOutput()),
          td(`class` := "layout")(
            vIcon(attr("small"), `class` := "mr-2", `@click` := "editItem(props.item)")("edit"),
            vIcon(attr("small"), `@click` := "deleteItem(props.item)")("delete")
          )
        )
      )
    ).render
  }

  private def libsImports() = {
    Seq(
      script(src := "https://cdn.jsdelivr.net/npm/vue/dist/vue.js"),
      script(src := "https://cdnjs.cloudflare.com/ajax/libs/axios/0.18.0/axios.min.js"),
      script(src := "https://cdn.jsdelivr.net/npm/vuetify/dist/vuetify.js"),
      script(src := "https://cdn.jsdelivr.net/npm/vuelidate@0.7.4/dist/vuelidate.min.js"),
      script(src := "/static/js/vue-virtual-scroll-list.js")
      //Todo: Use tui for markdown textfields
      //script(src := "https://cdnjs.cloudflare.com/ajax/libs/tui-editor/1.3.0/tui-editor-Editor.min.js"),
      //Todo: Use jsoneditor for json fields
      //script(src := "https://cdn.jsdelivr.net/npm/@json-editor/json-editor@latest/dist/jsoneditor.min.js")
    )
  }

  private def headImports() = {
    Seq(
      link(
        href := "https://fonts.googleapis.com/css?family=Roboto:100,300,400,500,700,900|Material+Icons",
        rel := "stylesheet"
      ),
      link(
        href := "https://cdn.jsdelivr.net/npm/vuetify/dist/vuetify.min.css",
        rel := "stylesheet"
      ),
      meta(
        name := "viewport",
        content := "width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no, minimal-ui"
      )
    )
  }
}

object ModelAdminTemplate
  extends ModelAdminTemplate