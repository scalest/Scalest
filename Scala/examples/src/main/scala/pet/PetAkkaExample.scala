package pet

import akka.actor.ActorSystem
import akka.http.scaladsl.server.HttpApp
import cats.implicits._
import scalest.admin.ModelAdmin
import scalest.admin.akka._
import scalest.admin.akka.tapir._
import scalest.admin.slick.health._
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile

import scala.concurrent.{ExecutionContext, Future}

object PetAkkaExample extends HttpApp with App with PetEnvironment with PetsModelAdmin {
  implicit lazy val system: ActorSystem = ActorSystem("PetAppSystem")
  implicit lazy val ec: ExecutionContext = system.dispatcher
  implicit lazy val dc: DatabaseConfig[H2Profile] = DatabaseConfig.forConfig[H2Profile]("slick", system.settings.config)
  val petRepository = new PetRepository[Future]
  val userRepository = new UserRepository[Future]
  val petsMA = ModelAdmin(petRepository, PetModel.uuidGenerator.some, Set(AdoptFuture(petRepository)))
  val userMA = ModelAdmin(userRepository, PetModel.uuidGenerator.some)
  val akkaAdminExtension =
    AkkaAdminExtension()
      .withModelAdmins(petsMA, userMA)
      .withHealthChecks(SlickHealthCheck(dc))
  val routes = akkaAdminExtension.routes
  Migration.migrate
  startServer("0.0.0.0", 9090, system)
}
