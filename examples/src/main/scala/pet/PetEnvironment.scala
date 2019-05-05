package pet

import akka.actor.ActorSystem
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile

import scala.concurrent.ExecutionContext

trait PetEnvironment {
  val system: ActorSystem
  implicit val dc: DatabaseConfig[H2Profile]
  implicit val ec: ExecutionContext
}
