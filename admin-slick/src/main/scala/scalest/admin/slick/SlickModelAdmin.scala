package scalest.admin.slick

import io.circe.{Decoder, Encoder}
import scalest.admin.{CrudRepository, ModelAdmin, ModelView}
import slick.basic.DatabaseConfig

import scala.concurrent.{ExecutionContext, Future}

object SlickModelAdmin {

  def apply(ea: EntityActions)
           (implicit dbConfig: DatabaseConfig[_],
            ec: ExecutionContext,
            modelEncoder: Encoder[ea.Model],
            modelDecoder: Decoder[ea.Model],
            idEncoder: Encoder[ea.Id],
            idDecoder: Decoder[ea.Id],
            mv: ModelView[ea.Model]): ModelAdmin[ea.Model, ea.Id] = {
    type Model = ea.Model
    type Id = ea.Id

    val crudRepository = new CrudRepository[Model, Id] {

      override def findAll(): Future[Seq[Model]] = dbConfig.db.run(ea.findAll)

      override def create(m: Model): Future[Id] = dbConfig.db.run(ea.create(m))

      override def update(m: Model): Future[Model] = dbConfig.db.run(ea.update(m))

      override def delete(ids: Set[ea.Id]): Future[Int] = dbConfig.db.run(ea.deleteByIds(ids))
    }

    new ModelAdmin(
      crudRepository = crudRepository,
      modelView = implicitly
    )
  }
}