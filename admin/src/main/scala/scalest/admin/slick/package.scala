package scalest.admin

import scalest.admin.slick.JdbcProfileProvider

package object admin {

  type H2ProfileProvider = JdbcProfileProvider.H2ProfileProvider
  type PostgresProfileProvider = JdbcProfileProvider.PostgresProfileProvider
  type DerbyProfileProvider = JdbcProfileProvider.DerbyProfileProvider
  type HsqlProfileProvider = JdbcProfileProvider.HsqlProfileProvider
  type MySQLProfileProvider = JdbcProfileProvider.MySQLProfileProvider
  type SQLLiteProfileProvider = JdbcProfileProvider.SQLLiteProfileProvider
}
