package scalest


package object admin
  extends FieldTypeViewInstances {

  //Todo: Auth
  final case class LoginRequest(username: String, password: String)

  type ModelRepr = Seq[FieldView]
}
