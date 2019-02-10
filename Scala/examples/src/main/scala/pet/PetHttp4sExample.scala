package pet

import cats.effect._
import cats.implicits._
import com.typesafe.config.{Config, ConfigFactory}
import org.http4s.HttpRoutes
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import pet.PetHttp4sExample.config
import pet.PetModel.Pet
import scalest.admin.element.{ElementsModelAdmin, ElementsModelService}
import scalest.admin.http4s._
import scalest.admin.page.{PagesModelAdmin, PagesModelService}
import scalest.admin.slick.health._
import scalest.admin.users.{UsersModelAdmin, UsersModelService}
import scalest.admin.versions._
import scalest.admin.{AdminExtension, ModelAdmin, SwaggerRoute}
import scalest.auth.{User, UserTokenCodec}
import scalest.health.HealthCheck
import scalest.service.GenId
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile

import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.util.Random

object PetHttp4sExample extends IOApp {
  val config: Config = ConfigFactory.load()
  implicit val genId: GenId[String] = GenId.genUUID
  implicit val userTokenCodec: UserTokenCodec = UserTokenCodec.JwtSecretCodec("secret")
  implicit val ec: ExecutionContext = global
  implicit val dc: DatabaseConfig[H2Profile] = DatabaseConfig.forConfig[H2Profile]("slick", config)
  val petsService = new PetModelService[IO]
  val usersService = new UsersModelService.Dummy[IO]
  val versionsService = new VersionsModelService.Dummy[IO]
  val pagesService = new PagesModelService.Dummy[IO]
  val elementsService = new ElementsModelService.Dummy[IO]

  val pagesMA = new PagesModelAdmin[IO](pagesService, elementsService, List.empty)
  val elementsMA = new ElementsModelAdmin[IO](elementsService, List.empty)
  val versionsMA = new VersionsModelAdmin[IO](versionsService)
  val usersMA = new UsersModelAdmin(usersService, List(versionsMA.extension[User, String]))
  val petsMA = new ModelAdmin(petsService, List(versionsMA.extension[Pet, String]))

  val adminExtension: AdminExtension[IO] =
    AdminExtension[IO]()
      .withModelAdmins(petsMA, usersMA, versionsMA, pagesMA, elementsMA)
      .withHealthChecks(
        SlickHealthCheck(dc),
        HealthCheck.make(
          IO(Random.nextBoolean()),
          "flakky",
          "Can be randomly alive",
        ),
        HealthCheck.make(
          IO.sleep(2.seconds).as(true),
          "timeout",
          "Always timeouts",
        ),
      )

  val routes: HttpRoutes[IO] = adminExtension.routes <+>
        SwaggerRoute(documentedEndpoints = adminExtension.endpoints) <+>
        Http4sAdminUI() <+> pagesMA.routes

  Migration.migrate

  override def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .withNio2(true)
      .bindHttp(9090, "0.0.0.0")
      .withHttpApp(routes.orNotFound)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}

object Test extends App {
  implicit lazy val ec: ExecutionContext = global
  implicit lazy val dc: DatabaseConfig[H2Profile] = DatabaseConfig.forConfig[H2Profile]("slick", config)

  val res = SlickAction[Int](
    Pure(2)
      .tap(println)
      .map(_ + 2)
      .tap(println)
      .flatMap[Int](_ => Fail(new RuntimeException("kek")))
      .tap(println),
  ).future
    .map(_ => ())

  res.onComplete(println)
  Await.result(res.recover(_ => ()), Duration.Inf)

  val b = 2 + 2
}
