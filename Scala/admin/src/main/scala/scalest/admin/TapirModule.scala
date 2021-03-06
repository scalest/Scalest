package scalest.admin

import sttp.tapir.server.ServerEndpoint

trait TapirModule[F[_]] {
  def endpoints: List[ServerEndpoint[_, _, _, Nothing, F]]
}
