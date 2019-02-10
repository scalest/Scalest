package scalest.admin

import cats.ApplicativeError
import cats.implicits._
import scalest.admin.dto._
import scalest.error
import scalest.tapir._
import sttp.tapir.server.ServerEndpoint

case class ProtocolModule[F[_]](modelAdmins: Seq[ModelAdmin[F, _, _]])(implicit C: ApplicativeError[F, Throwable])
    extends TapirModule[F] {
  val protocols: Seq[ModelProtocol] = modelAdmins.map(_.protocol)
  val protocolEndpoint: ServerEndpoint[Unit, error.CommonError, Seq[ModelProtocol], Nothing, F] = {
    commonEndpoint("protocol").get
      .in("admin" / "protocol")
      .out(jsonBody[Seq[ModelProtocol]])
      .tapir(_ => protocols.pure[F])
  }
  override val endpoints: List[ServerEndpoint[_, _, _, Nothing, F]] = List(protocolEndpoint)
}
