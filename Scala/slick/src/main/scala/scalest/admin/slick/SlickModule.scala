package scalest.admin.slick

import slick.jdbc.JdbcProfile

abstract class SlickModule[P <: JdbcProfile](override val profile: P) extends SlickSearchModule
  with SlickCrudRepositoryModule
  with SlickActionModule {
  override type Profile = P
}
