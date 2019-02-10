package pet

import akka.actor.ActorSystem
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import distage._
import scalest.ScalestApp
import scalest.admin.ModelViewInstances._
import scalest.admin.{ModelAdmin, ModelView}
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
  extends ScalestApp("PetApp") {

  private val module = PetModule ++ akkaModule
  private val plan = injector.plan(module)
  val classes: Locator = injector.produceUnsafe(plan)

  classes.get[Migration].migrate()

  val petRepository: PetRepository = classes.get[PetRepository]

  val petAdminExt = new ModelAdmin(
    "pet",
    List[(String, ModelView)]("id" -> intIdMV, "name" -> strMV, "adopted" -> boolMV),
    petRepository
  )

  override val routes = cors() {
    petAdminExt.route
  }
}
