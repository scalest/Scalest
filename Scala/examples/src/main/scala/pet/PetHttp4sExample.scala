package pet

import cats.effect._
import cats.implicits._
import com.typesafe.config.{Config, ConfigFactory}
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import scalest.admin.http4s._
import scalest.admin.http4s.tapir._
import scalest.admin.slick.health._
import scalest.admin.{FutureToEffect, ModelAdmin}
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile

import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ExecutionContext, Future}

object PetHttp4sExample extends IOApp with PetsModelAdmin {
  val config: Config = ConfigFactory.load()
  implicit val F2IO: FutureToEffect[IO] = new FutureToEffect[IO] {
    override def toEffect[A](future: Future[A]): IO[A] = IO.fromFuture(IO(future))
  }
  implicit lazy val ec: ExecutionContext = global
  implicit lazy val dc: DatabaseConfig[H2Profile] = DatabaseConfig.forConfig[H2Profile]("slick", config)
  val petRepository = new PetRepository[IO]
  val userRepository = new UserRepository[IO]
  val petsMA = ModelAdmin(petRepository, PetModel.uuidGenerator.some, Set(AdoptIO(petRepository)))
  val userMA = ModelAdmin(userRepository, PetModel.uuidGenerator.some)
  val adminExtension =
    Http4sAdminExtension()
      .withModelAdmins(petsMA, userMA)
      .withHealthChecks(SlickHealthCheck(dc))
  Migration.migrate
  override def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .withNio2(true)
      .bindHttp(9090, "0.0.0.0")
      .withHttpApp(CORS(adminExtension.routes.orNotFound))
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}
