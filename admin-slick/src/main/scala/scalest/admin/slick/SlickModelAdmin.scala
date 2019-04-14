package scalest.admin.slick

import io.circe.{Decoder, Encoder}
import scalest.admin.{ModelAdmin, ModelSchema}
import slick.basic.DatabaseConfig

import scala.concurrent.ExecutionContext

object SlickModelAdmin {

  def apply(sm: SlickModel)
           (implicit ms: ModelSchema[sm.Model],
            dc: DatabaseConfig[_],
            ec: ExecutionContext,
            me: Encoder[sm.Model],
            md: Decoder[sm.Model],
            ie: Encoder[sm.Id],
            id: Decoder[sm.Id]): ModelAdmin[sm.Model, sm.Id] = new ModelAdmin(SlickCrudRepository(sm), ms)
}

