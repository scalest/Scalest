package pet

import akka.actor.ActorSystem
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import distage._
import pet.PetModel.Sexes.Sex
import scalest.ScalestApp
import scalest.admin.ModelViewInstances._
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

  private val adminExtension = new AdminExtension(
    List(
      new ModelAdmin(
        "pet",
        List("id" -> intIdMV, "name" -> strMV, "adopted" -> boolMV, "sex" -> enumMV[Sex]),
        petRepository
      )
    )
  )


  override val routes = cors() {
    adminExtension.route
  }

  startServer()
}
