package scalest.admin

import cats.ApplicativeError
import cats.implicits._
import io.circe.syntax._
import scalest.admin.action._
import scalest.admin.dto._
import scalest.admin.pagination.{PageRequest, PageResponse}
import scalest.admin.schema.ModelSchema
import scalest.auth._
import scalest.exception.ApplicationException
import scalest.service.ModelService
import scalest.tapir._
import sttp.tapir.Codec.JsonCodec
import sttp.tapir.EndpointInput.Auth
import sttp.tapir.server.ServerEndpoint

import scala.language.implicitConversions

class ModelAdmin[F[_], M, I](
  service: ModelService[F, M, I],
  extensions: List[ModelExtension[F, M, I]],
)(
  implicit
  val MMS: ModelSchema[M],
  val MMD: EntityDescriptors[M],
  val IMD: EntityDescriptors[I],
  val UTC: UserTokenCodec,
  val F: ApplicativeError[F, Throwable],
) extends TapirModule[F] {
  val protocol: ModelProtocol = ModelProtocol(
    MMS.dto,
    service.actions.map(a => a.schema.dto.copy(name = a.name)),
    service.searchActions.map(a => a.schema.dto.copy(name = a.name)),
  )
  val userAuth: Auth.Http[User] =
    jwtAuth(
      UTC.decodeUser(_).toRight(ApplicationException("Cannot decode user")),
      UTC.encodeUser,
    )
  private val extension = extensions.foldLeft(ModelExtension.empty[F, M, I])(_.compose(_))
  private val searchLogic = (_: User, request: PageRequest) => service.findAll(request: PageRequest)
  private val createLogic = (u: User, m: M) => service.create(m) <* extension.afterCreate(u, m)
  private val updateLogic = (u: User, m: M) => service.upsert(m) <* extension.afterUpdate(u, m)
  private val deleteLogic = (u: User, ids: Seq[I]) => service.delete(ids).as(1) <* extension.afterDelete(u, ids)
  private val actionLogic = (_: User, r: ActionRequest) => {
    service.actions
      .find(_.name == r.name)
      .map(_.execute(r.data).getOrElse(F.pure(ActionFailure: ActionResponse)))
      .getOrElse(F.pure(ActionNotFound: ActionResponse))
  }

  private val searchEndpoint = {
    implicit val mpsjc: JsonCodec[PageResponse[M]] = MMD.pageResponseJsonCodec
    commonEndpoint(s"search ${MMS.name}").post
      .in(userAuth)
      .in("admin" / "api" / MMS.name / "search")
      .in(caseQuery[PageRequest])
      .out(jsonBody[PageResponse[M]])
      .tapir[F]((searchLogic.apply _).tupled)
  }

  private val createEndpoint = {
    import IMD.e
    implicit val mjc: JsonCodec[M] = {
      val decoderWithGeneratedId =
        MMD.decoder.prepare(c => c.focus.map(_.mapObject(_.add("id", service.genId.gen.asJson)).hcursor).getOrElse(c))
      MMD.copy(decoder = decoderWithGeneratedId)
    }.jsonCodec

    commonEndpoint(s"create ${MMS.name}").post
      .in(userAuth)
      .in("admin" / "api" / MMS.name / "create")
      .in(jsonBody[M])
      .out(jsonBody[M])
      .tapir[F]((createLogic.apply _).tupled)
  }

  private val updateEndpoint = {
    implicit val mjc: JsonCodec[M] = MMD.jsonCodec
    commonEndpoint(s"update ${MMS.name}").put
      .in(userAuth)
      .in("admin" / "api" / MMS.name / "update")
      .in(jsonBody[M])
      .out(jsonBody[M])
      .tapir[F]((updateLogic.apply _).tupled)
  }

  private val deleteEndpoint = {
    implicit val ijc: JsonCodec[Seq[I]] = IMD.seqJsonCodec
    commonEndpoint(s"delete ${MMS.name}").delete
      .in(userAuth)
      .in("admin" / "api" / MMS.name / "delete")
      .in(jsonBody[Seq[I]])
      .out(jsonBody[Int])
      .tapir[F]((deleteLogic.apply _).tupled)
  }

  private val actionEndpoint = {
    commonEndpoint(s"action for ${MMS.name}").put
      .in(userAuth)
      .in("admin" / "api" / MMS.name / "action")
      .in(jsonBody[ActionRequest])
      .out(jsonBody[ActionResponse])
      .tapir[F]((actionLogic.apply _).tupled)
  }

  override def endpoints: List[ServerEndpoint[_, _, _, Nothing, F]] =
    List(
      searchEndpoint,
      createEndpoint,
      updateEndpoint,
      deleteEndpoint,
      actionEndpoint,
    )
}
