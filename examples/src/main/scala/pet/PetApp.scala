package pet

import akka.actor.ActorSystem
import akka.http.scaladsl.server.HttpApp
import scalest.admin.slick.SlickModelAdmin
import scalest.admin.AdminExtension
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile

object PetApp extends HttpApp with App {

  val system = ActorSystem("PetAppSystem")

  import system.dispatcher

  implicit val dbConfig: DatabaseConfig[H2Profile] = DatabaseConfig.forConfig[H2Profile]("slick", system.settings.config)

  Migration.migrate

  override val routes = AdminExtension(SlickModelAdmin(Pets), SlickModelAdmin(Users)).route

  startServer("0.0.0.0", 9000, system)
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