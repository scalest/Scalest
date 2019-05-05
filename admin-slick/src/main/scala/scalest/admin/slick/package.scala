package scalest.admin

import scala.concurrent.Future
import _root_.slick.basic.DatabaseConfig
import _root_.slick.dbio.DBIO

package object slick {

  type H2ProfileProvider = JdbcProfileProvider.H2ProfileProvider
  type PostgresProfileProvider = JdbcProfileProvider.PostgresProfileProvider
  type DerbyProfileProvider = JdbcProfileProvider.DerbyProfileProvider
  type HsqlProfileProvider = JdbcProfileProvider.HsqlProfileProvider
  type MySQLProfileProvider = JdbcProfileProvider.MySQLProfileProvider
  type SQLLiteProfileProvider = JdbcProfileProvider.SQLLiteProfileProvider

  implicit class DbioOps[T](val action: DBIO[T]) extends AnyVal {
    def run(implicit dc: DatabaseConfig[_]): Future[T] = dc.db.run(action)
  }

}
