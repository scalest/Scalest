package scalest.admin

import scalatags.Text.all._
import scalest.admin.Vue._

trait ModelAdminTemplate {
  type Header = String
  type Template = String

  def generateSingleModelHtml(header: Header, ma: ModelAdmin[_]): String = {
    import ma.{modelName, script, template}
    html(
      head(headImports()),
      body(
        div(id := s"${modelName}App")(
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

  def generateHeader(mas: List[ModelAdmin[_]]): Header =
    vCard(attr("color") := "grey lighten-4", attr("flat"), height := "100px", attr("tile"))(
      tag("v-toolbar")(attr("dense"))(
        tag("v-toolbar-side-icon"),
        tag("v-toolbar-title")("Scalest Admin"),
        vSpacer,
        tag("v-menu")(vBind("nudge-width") := "100")(
          tag("v-toolbar-title")(attr("slot") := "activator")(
            span("Models"),
            tag("v-icon")("arrow_drop_down")
          ),
          tag("v-list")(
            for (ma <- mas) yield {
              tag("v-list-tile")(
                tag("v-list-tile-title")(
                  a(href := s"/admin/${ma.modelName}")(ma.modelName.capitalize)
                )
              )
            }
          )
        )
      )

    ).render

  def generateTemplate(ma: ModelAdmin[_]): Template = {
    import ma._
    vCard(
      vSnackbar(vModel := "notification")("""{{notificationText}}"""),
      vToolbar(attr("color") := "white", attr("flat"))(
        vToolbarTitle(
          s"${modelName}s".capitalize
        ),
        vDivider(`class` := "mx-2", attr("inset"), attr("vertical")),
        vSpacer(),
        vDialog(vModel := "dialog", maxWidth := "500px")(
          vBtn(
            attr("slot") := "activator",
            attr("color") := "primary",
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
                  vFlex(attr("xs12"), attr("sm6"), attr("md4"))(
                    for ((n, mf) <- modelViewRepr) yield raw(mf.toInput(n))
                  )
                )
              )
            ),
            vCardActions(
              vSpacer,
              vBtn(attr("color") := "blue darken-1", attr("flat"), `@click` := "close")("Cancel"),
              vBtn(attr("color") := "blue darken-1", attr("flat"), `@click` := "save")("Save")
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
          for ((n, mf) <- modelViewRepr) yield raw(mf.toOutput(n)),
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
}

object ModelAdminTemplate
  extends ModelAdminTemplate