package scalest.admin.page

import cats.MonadError
import cats.implicits._
import scalest.admin.element.ElementsModelService
import scalest.admin.{ModelAdmin, ModelExtension, ToRoute}
import scalest.auth.UserTokenCodec
import scalest.error
import scalest.tapir._
import sttp.tapir.server.ServerEndpoint

final class PagesModelAdmin[F[_]: MonadError[*[_], Throwable]](
  service: PagesModelService[F],
  elementService: ElementsModelService[F],
  extensions: List[ModelExtension[F, Page, String]],
)(
  implicit UTC: UserTokenCodec,
) extends ModelAdmin[F, Page, String](service, extensions) {
  val pageEndpoint: ServerEndpoint[String, error.CommonError, String, Nothing, F] =
    commonEndpoint("page").get
      .in(path[String]("page"))
      .out(htmlBodyUtf8)
      .tapir { page =>
        service
          .find(page)
          .flatMap(_.fold("Not found".pure[F]) { p =>
            elementService
              .findByIds(p.elements.toSet)
              .map(_.map(e => e.id -> e.content).toMap)
              .map(map => p.elements.flatMap(map.get).mkString)
          })
      }

  def routes[R](implicit toRoute: ToRoute[F, R]): R = toRoute(List(pageEndpoint))
}
