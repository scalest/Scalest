package scalest.admin

import scalest.admin.schema.ModelSchema
import scalest.auth.User
import scalest.tapir.EntityDescriptors

package object users {
  implicit val modelSchema: ModelSchema[User] = ModelSchema.gen[User]
  implicit val modelED: EntityDescriptors[User] = EntityDescriptors.create
}
