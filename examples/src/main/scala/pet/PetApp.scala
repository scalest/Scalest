package pet

import akka.actor.ActorSystem
import akka.http.scaladsl.server.HttpApp
import scalest.admin.akka.AkkaAdminExtension
import scalest.admin.slick._
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile

import scala.concurrent.ExecutionContext

object PetApp extends HttpApp with App with PetEnvironment with PetsModelAdmin {
  lazy val system = ActorSystem("PetAppSystem")
  lazy implicit val ec: ExecutionContext = system.dispatcher
  lazy implicit val dc: DatabaseConfig[H2Profile] = DatabaseConfig.forConfig[H2Profile]("slick", system.settings.config)

  Migration.migrate

  override val routes = AkkaAdminExtension(petsMA, SlickRepository(Users)).route

  startServer("0.0.0.0", 9000, system)
}