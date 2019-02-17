package scalest.admin.slick


case class Lens[O, V](get: O => V, set: (O, V) => O)

object Lens {
  def lens[O, V](get: O => V)(set: (O, V) => O) = Lens(get, set)
}