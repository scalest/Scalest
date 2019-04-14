package scalest.admin

import akka.http.scaladsl.server.{Directives, Route}
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import io.circe.{Decoder, Encoder}

class ModelAdmin[Model: Encoder : Decoder, Id: Encoder : Decoder](val crudRepository: CrudRepository[Model, Id], val modelSchema: ModelSchema[Model])
  extends Directives with ErrorAccumulatingCirceSupport {

  val route: Route = pathPrefix(s"${modelSchema.name}") {
    (get & pathEndOrSingleSlash) {
      complete(crudRepository.findAll())
    } ~
      (post & pathEndOrSingleSlash) {
        entity(as[Model])(m => complete(crudRepository.create(m)))
      } ~
      (put & pathEndOrSingleSlash) {
        entity(as[Model])(m => complete(crudRepository.update(m)))
      } ~
      (delete & pathEndOrSingleSlash) {
        entity(as[Set[Id]])(ids => complete(crudRepository.delete(ids)))
      }
  }
}

object ModelAdmin {
  def apply[Model: Encoder : Decoder : ModelSchema, Id: Encoder : Decoder](crudRepository: CrudRepository[Model, Id]): ModelAdmin[Model, Id] = {
    new ModelAdmin(crudRepository, modelSchema = implicitly)
  }
}