package scalest.service

import java.util.UUID

trait GenId[I] {
  def gen: I
}

object GenId {
  def apply[I](implicit genId: GenId[I]): GenId[I] = genId
  val genUUID: GenId[String] = new GenId[String] {
    override def gen: String = UUID.randomUUID.toString
  }
}
