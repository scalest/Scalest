//package pet
//
//import akka.actor.ActorSystem
//import akka.http.scaladsl.server.HttpApp
//import cats.effect.IO
//import cats.implicits._
//import pet.PetHttp4sExample.config
//import scalest.admin.akka._
//import scalest.admin.slick.health._
//import scalest.admin.users.{UsersModelAdmin, UsersModelService}
//import scalest.admin.versions.{VersionsModelAdmin, VersionsModelService}
//import scalest.admin.{AdminExtension, FutureToEffect, ModelAdmin, SwaggerRoute}
//import scalest.auth.UserTokenCodec
//import scalest.health.HealthCheck
//import scalest.service.GenId
//import slick.basic.DatabaseConfig
//import slick.jdbc.H2Profile
//
//import scala.concurrent.ExecutionContext.global
//import scala.concurrent.{ExecutionContext, Future}
//import scala.util.Random
//
//object PetAkkaExample extends HttpApp with App {
//  implicit lazy val system: ActorSystem = ActorSystem("PetAppSystem")
//  implicit lazy val ec: ExecutionContext = system.dispatcher
//  implicit lazy val dc: DatabaseConfig[H2Profile] = DatabaseConfig.forConfig[H2Profile]("slick", system.settings.config)
//  implicit val genId: GenId[String] = GenId.genUUID
//  implicit val userTokenCodec: UserTokenCodec = UserTokenCodec.JwtSecretCodec("secret")
//  val petsService = new PetModelService[Future]
//  val usersService = new UsersModelService.Dummy[Future]
//  val versionsService = new VersionsModelService.Dummy[Future]
//  val versionsMA = new VersionsModelAdmin[Future](versionsService)
//  val usersMA =  new UsersModelAdmin(usersService).withExtension(versionsMA.extension)
//  val petsMA = new ModelAdmin(petsService).withExtension(versionsMA.extension)
//  val adminExtension: AdminExtension[Future] =
//    AdminExtension[Future]()
//      .withModelAdmins(petsMA, usersMA, versionsMA)
//      .withHealthChecks(
//        SlickHealthCheck(dc),
//        HealthCheck.makeFuture(
//          () => {
//            println("asd")
//            Future(Random.nextBoolean())
//          },
//          "flakky",
//          "Can be randomly alive",
//        ),
//        HealthCheck.makeFuture(
//          () =>
//            Future.unit.map { _ =>
//              Thread.sleep(2000)
//              true
//            },
//          "timeout",
//          "Always timeouts",
//        ),
//      )
//
//  private val adminRoutes = adminExtension.routes
//  val routes = adminRoutes ~
//        SwaggerRoute(documentedEndpoints = adminExtension.endpoints) ~
//        AkkaAdminUI()
//
//  Migration.migrate
//  startServer("0.0.0.0", 9090, system)
//}
