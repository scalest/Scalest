package scalest.admin

import cats.ApplicativeError
import cats.implicits._
import scalest.admin.dto._
import scalest.error
import scalest.tapir._
import sttp.tapir.server.ServerEndpoint

case class AdminModule[F[_]](modelAdmins: Seq[ModelAdmin[F, _, _, _]])(implicit C: ApplicativeError[F, Throwable]) extends TapirModule[F] {
  val schemas: Seq[ModelInfoDto] = modelAdmins.map(_.info)

  val infoEndpoint: ServerEndpoint[Unit, error.CommonError, Seq[ModelInfoDto], Nothing, F] = {
    commonEndpoint("info").get
      .in("admin" / "info")
      .out(jsonBody[Seq[ModelInfoDto]])
      .tapir(_ => schemas.pure[F])
  }

  val availableInfoEndpoint: ServerEndpoint[Unit, error.CommonError, Seq[String], Nothing, F] = {
    commonEndpoint("availableInfo").get
      .in("admin" / "info" / "available")
      .out(jsonBody[Seq[String]])
      .tapir(_ => schemas.map(_.schema.name).pure[F])
  }

  override val routes: List[ServerEndpoint[_, _, _, Nothing, F]] = List(infoEndpoint, availableInfoEndpoint)
}
