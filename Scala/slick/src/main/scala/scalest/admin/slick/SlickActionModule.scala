package scalest.admin.slick

import scalest.admin.FutureToEffect
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

trait SlickActionModule {
  type Profile <: JdbcProfile
  val profile: Profile
  import profile.api._

  case class SlickAction[A](dbio: DBIO[A])(
    implicit
    val dc: DatabaseConfig[Profile],
    val ec: ExecutionContext,
    val rp: RecoverProvider = RecoverProvider.Implicits.noRecover
  ) {
    def flatMap[B](f: A => SlickAction[B]): SlickAction[B] = SlickAction(dbio.flatMap(f(_).dbio))
    def map[B](f: A => B): SlickAction[B] = SlickAction(dbio.map(f(_)))
  }

  trait RecoverProvider {
    def recover[U]: PartialFunction[Throwable, Future[U]]
  }

  object RecoverProvider {
    def apply(f: PartialFunction[Throwable, Throwable]): RecoverProvider = new RecoverProvider() {
      override def recover[U]: PartialFunction[Throwable, Future[U]] = RecoverProvider.lift(f)
    }

    def lift[U](errorHandler: PartialFunction[Throwable, Throwable]): PartialFunction[Throwable, Future[U]] = {
      case t => Future.failed(errorHandler(t))
    }

    object Implicits {
      implicit val noRecover: RecoverProvider = RecoverProvider(PartialFunction.empty)
    }

  }

  trait SlickComponent {
    implicit val dc: DatabaseConfig[Profile]
    implicit val ec: ExecutionContext
  }

  implicit class DBIOOps[A](dbio: DBIO[A])
                           (implicit
                            dc: DatabaseConfig[Profile],
                            ec: ExecutionContext,
                            rp: RecoverProvider = RecoverProvider.Implicits.noRecover) {
    def slickAction: SlickAction[A] = SlickAction(dbio)
  }

  implicit class SlickActionOps[A, P <: JdbcProfile](val action: SlickAction[A]) {

    def future: Future[A] = {
      import action._
      dc.db.run(dbio).recoverWith(rp.recover)(ec)
    }

    def effect[F[_]](implicit F2E: FutureToEffect[F]): F[A] = F2E.toEffect(future)
  }

}
