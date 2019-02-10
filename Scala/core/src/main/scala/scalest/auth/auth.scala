package scalest

import java.util.Base64

import io.circe.generic.JsonCodec
import io.circe.generic.extras.{Configuration, ConfiguredJsonCodec}
import scalest.admin.schema.{FieldSchema, FieldTypeSchemaInstances}

import scala.util.matching.Regex

package object auth {
  implicit private val configuration: Configuration = Configuration.default.withDiscriminator("$type")

  val AdminUsername: String = "admin"
  val AdminPassword: String = "nimda"
  val Token: String = Base64.getEncoder.encodeToString(s"$AdminUsername:$AdminPassword".getBytes)
  val CredentialsIncorrect = "error.credentials.incorrect"

  @ConfiguredJsonCodec
  final case class AuthRequest(username: String, password: String)

  @ConfiguredJsonCodec
  case class AuthResponse(token: String)

  @ConfiguredJsonCodec
  sealed trait Permission
  object Permission {
    val ModelActionRegex: Regex = "/admin/api/(.+)/(.+)".r
    val SwaggerRegex: Regex = "/api/swagger(\\.json|\\.yaml)?".r

    def fromPath(path: String): Option[Permission] = path match {
      case ModelActionRegex(model, "update") => Some(UpdatePermission(model))
      case ModelActionRegex(model, "delete") => Some(DeletePermission(model))
      case ModelActionRegex(model, "search") => Some(SearchPermission(model))
      case ModelActionRegex(model, "create") => Some(CreatePermission(model))
      case SwaggerRegex()                    => Some(SwaggerPermission)
      case "/health"                         => Some(HealthPermission)
      case _                                 => None
    }

    case class UpdatePermission(model: String) extends Permission
    case class DeletePermission(model: String) extends Permission
    case class CreatePermission(model: String) extends Permission
    case class SearchPermission(model: String) extends Permission
    case class PagePermission(page: String) extends Permission
    case object SwaggerPermission extends Permission
    case object HealthPermission extends Permission
    implicit val fieldType: FieldSchema[Permission] = FieldTypeSchemaInstances.jsonFTS.copy()
  }

  @JsonCodec
  case class User(
    id: String,
    username: String,
    password: String,
    permissions: List[Permission],
    isSuperUser: Boolean = false,
  ) {
    def hasPermission(permission: Permission): Boolean = isSuperUser || permissions.contains(permission)
  }

}
