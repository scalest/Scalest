package scalest.tapir

import cats.ApplicativeError
import cats.implicits._
import sttp.tapir.docs.openapi.TapirOpenAPIDocs
import sttp.tapir.openapi.TapirOpenAPICirceEncoders
import sttp.tapir.openapi.circe.yaml.TapirOpenAPICirceYaml
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.{Endpoint, Tapir}

trait TapirCommon extends Tapir
  with TapirJsonCirce
  with TapirCommonCombinators
  with TapirJwtAuth
  with TapirServerCommon
  with TapirOpenAPIDocs
  with TapirOpenAPICirceYaml
  with TapirOpenAPICirceEncoders
  with TapirCommonInstances {

  implicit class TapirEndpointsOps[I, E, O](endpoint: Endpoint[I, E, O, Nothing]) {
    def tapir[F[_]](logic: I => F[O])
                   (implicit EEH: EffectErrorHandler[E], AE: ApplicativeError[F, Throwable]): ServerEndpoint[I, E, O, Nothing, F] = {
      ServerEndpoint(endpoint, logic(_).attempt.map(_.leftMap(EEH.handle)))
    }
  }

}
