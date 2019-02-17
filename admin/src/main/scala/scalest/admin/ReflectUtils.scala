package scalest.admin

import scala.reflect.runtime.universe._

object ReflectUtils {

  private val mirror: Mirror = runtimeMirror(getClass.getClassLoader)

  def getEnum[T <: Enumeration#Value : TypeTag]: Enumeration = {
    typeOf[T] match {
      case TypeRef(enumType, _, _) =>
        val moduleSymbol = enumType.termSymbol.asModule
        mirror.reflectModule(moduleSymbol).instance.asInstanceOf[Enumeration]
    }
  }
}
