package pet

import akka.actor.ActorSystem
import distage._
import scalest.ScalestApp
import scalest.admin.slick.SlickModelAdmin
import scalest.admin.AdminExtension
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile

object PetModule
  extends ModuleDef {
  make[DatabaseConfig[H2Profile]].from { system: ActorSystem =>
    DatabaseConfig.forConfig[H2Profile]("slick", system.settings.config)
  }
  make[Migrator]
}

object PetApp extends ScalestApp("PetApp", List(PetModule)) with App {

  import system.dispatcher

  implicit val dbConfig: DatabaseConfig[H2Profile] = locator.get[DatabaseConfig[H2Profile]]

  val migrator: Migrator = locator.get[Migrator]

  migrator.migrate()

  override val routes = AdminExtension(SlickModelAdmin(Pets), SlickModelAdmin(Users)).route

  startServer()
}

/* You could also create ModelView manually like this:
  import scalest.admin._

  implicit val modelView = ModelView[Pet](
    "pet",
    Seq(
      //Can change write flag, read flag, parse function and default value
      FieldView("id", intFTV, writeable = false),
      FieldView("name", strFTV),
      FieldView("adopted", boolFTV),
      FieldView("sex", enumFTV[Sex]),
      FieldView("tags", seqFTV[Sex]),
      FieldView("bodySize", byteFTV)
    )
  )
*/