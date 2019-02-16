package scalest.admin

import akka.http.scaladsl.server.Route
import io.circe.{Decoder, Encoder}
import scalest.ScalestService
import scalest.json.CirceJsonSupport

//Todo: Entities relations
class ModelAdmin[Model: Encoder : Decoder](val modelName: String,
                                           val modelViewRepr: List[(String, ModelView)],
                                           val crudRepository: CrudRepository[Model])
  extends ScalestService with ModelAdminTemplate with ModelAdminScript with CirceJsonSupport {

  val template: Template = generateTemplate(this)

  val script: String = generateScript(this)

  //Todo: Server-Side Pagination operation
  //Todo: Search operation
  //Todo: Custom Actions endpoint operation
  //Todo: Delete Selected operation
  //Todo: Entity Validation using annotations
  val route: Route = pathPrefix("pets") {
    (get & pathEndOrSingleSlash) {
      complete(crudRepository.findAll())
    } ~
      (post & pathEndOrSingleSlash) {
        entity(as[Model])(m => complete(crudRepository.create(m)))
      } ~
      (put & pathEndOrSingleSlash) {
        entity(as[Model])(m => complete(crudRepository.upsert(m)))
      } ~ //Todo: Support for More Id types, probably use delete all like interface
      (delete & pathPrefix(IntNumber) & pathEndOrSingleSlash) { id =>
        complete(crudRepository.delete(id))
      } ~
      (delete & pathEndOrSingleSlash) {
        entity(as[Seq[Int]])(ids => complete(crudRepository.deleteAll(ids)))
      }
  }
}