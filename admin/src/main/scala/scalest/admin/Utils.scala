package scalest.admin

import scala.reflect.runtime.universe._

object Utils {

  def snakify(name: String): String = name.replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2").replaceAll("([a-z\\d])([A-Z])", "$1_$2").toLowerCase

  private val mirror: Mirror = runtimeMirror(getClass.getClassLoader)

  def getEnum[T <: Enumeration#Value : TypeTag]: Enumeration = {
    typeOf[T] match {
      case TypeRef(enumType, _, _) =>
        val moduleSymbol = enumType.termSymbol.asModule
        mirror.reflectModule(moduleSymbol).instance.asInstanceOf[Enumeration]
    }
  }

}
