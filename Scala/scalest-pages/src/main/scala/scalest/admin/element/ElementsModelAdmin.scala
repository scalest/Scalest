package scalest.admin.element

import cats.ApplicativeError
import scalest.admin.element.ElementModel._
import scalest.admin.{ModelAdmin, ModelExtension}
import scalest.auth.UserTokenCodec

final class ElementsModelAdmin[F[_]: ApplicativeError[*[_], Throwable]](
  service: ElementsModelService[F],
  extensions: List[ModelExtension[F, Element, String]],
)(
  implicit UTC: UserTokenCodec,
) extends ModelAdmin[F, Element, String](service, extensions)
