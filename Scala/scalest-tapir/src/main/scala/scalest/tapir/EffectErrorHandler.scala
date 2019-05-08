package scalest.tapir

trait EffectErrorHandler[E] {
  def handle(t: Throwable): E
}
