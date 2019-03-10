package pet

import akka.actor.ActorSystem
import distage._
import scalest.ScalestApp
import scalest.admin.{AdminExtension, ModelAdmin}
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile

object PetModule
  extends ModuleDef {
  make[PetRepository]
  make[DatabaseConfig[H2Profile]].from { system: ActorSystem =>
    DatabaseConfig.forConfig[H2Profile]("slick", system.settings.config)
  }
  make[Migration]
}

object PetApp
  extends ScalestApp("PetApp", List(PetModule)) with App {

  locator.get[Migration].migrate()

  val petRepository: PetRepository = locator.get[PetRepository]

  /*//Manual modelView creation
  import scalest.admin._

  implicit val modelView = ModelView[Pet](
    "pet",
    Seq(
      //Can change write flag, read flag, parse function and default value
      FieldView("id", intFTV, writeable = false),
      FieldView("name", strFTV),
      FieldView("adopted", boolFTV),
      FieldView("sex", enumFTV[Sex])
    )
  )*/

  //Automatic modelView generation all can be customized using annotations
  override val routes = new AdminExtension(ModelAdmin(petRepository)).route

  startServer()
}
