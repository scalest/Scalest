package scalest.admin

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Route
import io.circe.{Decoder, Encoder}
import scalest.ScalestService
import scalest.json.CirceJsonSupport

class ModelAdmin[Model: Encoder : Decoder](modelName: String,
                                           modelViewRepr: List[(String, ModelView)],
                                           crudRepository: CrudRepository[Model])
  extends ScalestService with ModelAdminTemplate with CirceJsonSupport {

  val template: String = generate(modelName, modelViewRepr)

  val route: Route =
    pathPrefix("admin") {
      pathPrefix("pets") {
        complete(
          HttpEntity(ContentTypes.`text/html(UTF-8)`, template)
        )
      }
    } ~ pathPrefix("api") {
      pathPrefix("pets") {
        (get & pathEndOrSingleSlash) {
          complete(crudRepository.findAll())
        } ~
          (post & pathEndOrSingleSlash) {
            entity(as[Model])(m => complete(crudRepository.create(m)))
          } ~
          (put & pathEndOrSingleSlash) {
            entity(as[Model])(m => complete(crudRepository.upsert(m)))
          } ~
          (delete & pathPrefix(IntNumber) & pathEndOrSingleSlash) { id =>
            complete(crudRepository.delete(id))
          } ~
          (delete & pathEndOrSingleSlash) {
            entity(as[Seq[Int]])(ids => complete(crudRepository.deleteAll(ids)))
          }
      }
    }
}