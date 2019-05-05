package scalest.admin.akka

import akka.http.scaladsl.server.Route
import io.circe.{Decoder, Encoder}
import scalest.admin.{ModelAction, ModelActionCommand, ModelInfo, ModelRepository, ModelSchema}

import scala.concurrent.Future

class AkkaModelAdmin[Model, Id](val repository: ModelRepository[Model, Id],
                                val schema: ModelSchema[Model],
                                val actions: Set[ModelAction[Model, Id]] = Set.empty[ModelAction[Model, Id]])
                               (implicit em: Encoder[Model], dm: Decoder[Model], ei: Encoder[Id], di: Decoder[Id])
  extends HttpService {

  val info = ModelInfo(schema, actions.map(_.name))

  val route: Route = pathPrefix(s"${schema.name}") {
    findAllRoute ~
      createRoute ~
      updateRoute ~
      deleteRoute ~
      actionRoute
  }

  def findAllRoute: Route = get {
    pathEndOrSingleSlash {
      complete(repository.findAll())
    }
  }

  def createRoute: Route = post {
    pathEndOrSingleSlash {
      entity(as[Model])(m => complete(repository.create(m)))
    }
  }

  def updateRoute: Route = put {
    pathEndOrSingleSlash {
      entity(as[Model])(m => complete(repository.update(m)))
    }
  }

  def deleteRoute: Route = delete {
    pathEndOrSingleSlash {
      entity(as[Set[Id]])(ids => complete(repository.delete(ids)))
    }
  }

  def actionRoute: Route = post {
    pathPrefix("action") {
      pathEndOrSingleSlash {
        entity(as[ModelActionCommand[Id]]) { action =>
          complete(actionHandler(action))
        }
      }
    }
  }

  val actionCmdHandler: PartialFunction[ModelActionCommand[Id], Future[Seq[Model]]] = actionHandler orElse unhandled

  def actionHandler: PartialFunction[ModelActionCommand[Id], Future[Seq[Model]]] = actions.map { action =>
    val handler: PartialFunction[ModelActionCommand[Id], Future[Seq[Model]]] = {
      case ModelActionCommand(action.name, ids: Set[Id]) => action.handler(ids)
    }

    handler
  }.fold(PartialFunction.empty)(_ orElse _)

  def unhandled: PartialFunction[ModelActionCommand[Id], Future[Seq[Model]]] = {
    case action =>
      println(s"UNHANDLED MODEL ACTION - $action")
      Future.successful(Seq())
  }
}

object AkkaModelAdmin {

  def apply[Model, Id](repository: ModelRepository[Model, Id],
                       actions: Set[ModelAction[Model, Id]] = Set.empty[ModelAction[Model, Id]])
                      (implicit modelSchema: ModelSchema[Model],
                       em: Encoder[Model],
                       dm: Decoder[Model],
                       ei: Encoder[Id],
                       di: Decoder[Id]): AkkaModelAdmin[Model, Id] = {
    new AkkaModelAdmin(repository, modelSchema, actions)
  }

  implicit def repo2ma[Model, Id](repository: ModelRepository[Model, Id])
                                 (implicit modelSchema: ModelSchema[Model],
                                  em: Encoder[Model],
                                  dm: Decoder[Model],
                                  ei: Encoder[Id],
                                  di: Decoder[Id]): AkkaModelAdmin[Model, Id] = {
    new AkkaModelAdmin(repository, modelSchema)
  }
}