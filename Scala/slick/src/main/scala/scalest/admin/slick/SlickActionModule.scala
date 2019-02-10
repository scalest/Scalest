package scalest.admin.slick

import java.util.concurrent.Executors

import scalest.admin.FutureToEffect
import slick.basic.DatabaseConfig

import scala.concurrent.{ExecutionContext, Future}

trait SlickActionModule extends SlickModuleBase {
  import profile.api._

  private val slickIOEC: ExecutionContext = ExecutionContext.fromExecutor(Executors.newWorkStealingPool())

  implicit class DBIOOps[A](dbio: DBIO[A])(
    implicit
    dc: DatabaseConfig[Profile],
    ec: ExecutionContext,
  ) {
    def slickAction: SlickAction[A] = SlickAction[A](OfDBIO(dbio))
  }

  sealed trait SlickIO[+A] {
    def dbio: DBIO[A]
    def tap(f: A => Any): SlickIO[A] = map { v => f(v); v }
    def map[B](f: A => B): SlickIO[B] = SMap(f, this)
    def flatMap[B](f: A => SlickIO[B]): SlickIO[B] = Bind(f, this)
  }

  case class Pure[A](value: A) extends SlickIO[A] {
    override def dbio: DBIO[A] = DBIO.successful(value)
  }

  case class SMap[A, B](f: A => B, action: SlickIO[A]) extends SlickIO[B] {
    override def dbio: DBIO[B] = action.dbio.map(f)(slickIOEC)
  }

  case class Bind[A, B](f: A => SlickIO[B], action: SlickIO[A]) extends SlickIO[B] {
    override def dbio: DBIO[B] = action.dbio.flatMap(f(_).dbio)(slickIOEC)
  }

  case class OfDBIO[A](dbio: DBIO[A]) extends SlickIO[A]

  case class Fail(error: Throwable) extends SlickIO[Nothing] {
    override def dbio: DBIO[Nothing] = DBIO.failed(error)
  }

  case class SlickAction[A](sio: SlickIO[A])(implicit dc: DatabaseConfig[Profile], ec: ExecutionContext) {
    def dbio: DBIO[A] = sio.dbio
    def future: Future[A] = dc.db.run(sio.dbio)
    def effect[F[_]](implicit F2E: FutureToEffect[F]): F[A] = F2E.toEffect(future)
    def unit: SlickAction[Unit] = SlickAction(sio.map(_ => ()))
    def transactionally: SlickAction[A] = SlickAction(OfDBIO(dbio.transactionally))
  }

}
