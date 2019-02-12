package scalest.admin

import akka.http.scaladsl.server.Route
import io.circe.{Decoder, Encoder}
import scalest.ScalestService
import scalest.json.CirceJsonSupport

class ModelAdmin[Model: Encoder : Decoder](val modelName: String,
                                           val modelViewRepr: List[(String, ModelView)],
                                           val crudRepository: CrudRepository[Model])
  extends ScalestService with ModelAdminTemplate with CirceJsonSupport {

  val template: String = generateTemplate(modelName, modelViewRepr)

  val html: String = generateHtml(this)

  val apiRoute: Route =
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

  val route: Route = apiRoute
}