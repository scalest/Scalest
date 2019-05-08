package scalest.admin

import cats.ApplicativeError
import io.circe.syntax._
import io.scalaland.chimney.dsl.TransformerOps
import scalest.admin.crud.CrudRepository
import scalest.admin.dto._
import scalest.admin.pagination.{PageRequest, PageResponse}
import scalest.admin.schema.ModelSchema
import scalest.error
import scalest.tapir._
import sttp.tapir.Codec.JsonCodec
import sttp.tapir.server.ServerEndpoint

import scala.language.implicitConversions

case class ModelAdmin[F[_], M, Q, I](repository: CrudRepository[F, M, Q, I],
                                     idGenerator: Option[() => I] = None,
                                     actions: Set[ModelAction[F, M, I]] = Set.empty[ModelAction[F, M, I]])
                                    (implicit
                                     val schema: ModelSchema[M],
                                     val mmd: EntityDescriptors[M],
                                     val qmd: EntityDescriptors[Q],
                                     val imd: EntityDescriptors[I],
                                     AE: ApplicativeError[F, Throwable]) extends TapirModule[F] {
  val info: ModelInfoDto = ModelInfoDto(schema.transformInto[ModelSchemaDto], actions.map(_.name))
  private val searchLogic = repository.findBy(_: Q, _: PageRequest)
  private val createLogic = repository.create(_: M)
  private val updateLogic = repository.upsert(_: M)
  private val deleteLogic = repository.deleteByIds(_: Seq[I])

  val searchEndpoint: ServerEndpoint[(Q, PageRequest), error.CommonError, PageResponse[M], Nothing, F] = {
    implicit val qjc: JsonCodec[Q] = qmd.jsonCodec
    implicit val mpsjc: JsonCodec[PageResponse[M]] = mmd.pageResponseJsonCodec
    commonEndpoint(s"search ${schema.name}").post
      .in("admin" / "api" / schema.name / "search")
      .in(jsonBody[Q])
      .in(caseQuery[PageRequest])
      .out(jsonBody[PageResponse[M]])
      .tapir(searchLogic.tupled)
  }

  val createEndpoint: ServerEndpoint[M, error.CommonError, M, Nothing, F] = {
    import imd.e
    implicit val mjc: JsonCodec[M] =
      idGenerator
        .map { g =>
          val decoderWithGeneratedId = mmd.decoder.prepare(c => c.focus.map(_.mapObject(_.add("id", g().asJson)).hcursor).getOrElse(c))
          mmd.copy(decoder = decoderWithGeneratedId)
        }
        .getOrElse(mmd)
        .jsonCodec
    commonEndpoint(s"create ${schema.name}").post
      .in("admin" / "api" / schema.name)
      .in(jsonBody[M])
      .out(jsonBody[M])
      .tapir(createLogic)
  }

  val updateEndpoint: ServerEndpoint[M, error.CommonError, M, Nothing, F] = {
    implicit val mjc: JsonCodec[M] = mmd.jsonCodec
    commonEndpoint(s"update ${schema.name}").put
      .in("admin" / "api" / schema.name)
      .in(jsonBody[M])
      .out(jsonBody[M])
      .tapir(updateLogic)
  }

  val deleteEndpoint: ServerEndpoint[Seq[I], error.CommonError, Int, Nothing, F] = {
    implicit val ijc: JsonCodec[Seq[I]] = imd.seqJsonCodec
    commonEndpoint(s"delete ${schema.name}").delete
      .in("admin" / "api" / schema.name)
      .in(jsonBody[Seq[I]])
      .out(jsonBody[Int])
      .tapir(deleteLogic)
  }

  override val routes: List[ServerEndpoint[_, _, _, Nothing, F]] = List(searchEndpoint, createEndpoint, updateEndpoint, deleteEndpoint)
}
