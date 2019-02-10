package scalest.admin.slick

import slick.jdbc.JdbcProfile

trait SlickModuleBase {
  type Profile <: JdbcProfile
  val profile: Profile
}
