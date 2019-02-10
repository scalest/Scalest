package scalest.migrate

import cats.{Applicative, ApplicativeError}
import cats.implicits._

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

trait Migration[F[_]] {
  def name: String
  def version: String
  def migrate: F[Unit]
  def rollback: F[Unit]
}

sealed trait MigrationResult
case object MigrationSuccess extends MigrationResult
case class MigrationFailure(reason: String) extends MigrationResult

object V1Migration extends Migration[Future] {
  override def name: String = "First one"
  override def version: String = "0.0.1"
  override def migrate: Future[Unit] = Future(println("First")) *> Future.unit
  override def rollback: Future[Unit] = Future.unit
}

object V2Migration extends Migration[Future] {
  override def name: String = "Second one"
  override def version: String = "0.0.2"
  override def migrate: Future[Unit] = Future(println("Second")) *> Future.unit
  override def rollback: Future[Unit] = Future.unit
}

case class MigrationHistory(
  version: String,
  name: String,
)

trait MigrationHistoryController[F[_]] {
  def exists(migration: Migration[F]): F[Boolean]
}

object Migrate {
  def apply[F[_]](
    migrations: Migration[F]*,
  )(implicit AE: ApplicativeError[F, Throwable], controller: MigrationHistoryController[F]): F[MigrationResult] =
    migrations
      .foldLeft(Applicative[F].unit)((f, m) => controller.exists(m).map(if (_) f else f *> m.migrate))
      .map[MigrationResult](_ => MigrationSuccess)
      .recover {
        case t => MigrationFailure(t.getMessage)
      }
}

object MigrationTest extends App {
  implicit val controller: MigrationHistoryController[Future] = new MigrationHistoryController[Future] {
    override def exists(migration: Migration[Future]): Future[Boolean] = migration match {
      case V1Migration => Future(true)
      case V2Migration => Future(false)
    }
  }
  val migration = Migrate(V1Migration, V2Migration)
  migration.foreach(println)
  Await.ready(migration, Duration.Inf)
}
