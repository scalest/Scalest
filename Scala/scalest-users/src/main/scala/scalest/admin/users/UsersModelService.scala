package scalest.admin.users

import cats.effect.Sync
import cats.implicits.{catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId, toFlatMapOps}
import scalest.auth._
import scalest.exception.ApplicationException
import scalest.service.{GenId, ModelService}

trait UsersModelService[F[_]] extends ModelService[F, User, String] {
  def login(username: String, password: String): F[User]
}

object UsersModelService {
  implicit val genUserId: GenId[String] = GenId.genUUID

  class Dummy[F[_]: Sync] extends ModelService.Dummy[F, User, String](_.id) with UsersModelService[F] {
    override def login(username: String, password: String): F[User] =
      if (username == AdminUsername && password == AdminPassword)
        User(genUserId.gen, AdminUsername, AdminPassword, List.empty, isSuperUser = true).pure[F]
      else
        find(u => u.username == username && u.password == password)
          .flatMap(_.fold(ApplicationException(CredentialsIncorrect).raiseError[F, User])(_.pure[F]))
  }
}
