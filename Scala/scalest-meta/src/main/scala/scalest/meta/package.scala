package scalest

import scala.reflect.macros.whitebox
import scala.reflect.runtime.universe._

package object meta {
  def reflectOuterEnum[E <: Enumeration#Value: WeakTypeTag]: Enumeration = {
    val mirror: Mirror = runtimeMirror(getClass.getClassLoader)

    weakTypeTag[E].tpe match {
      case TypeRef(enumType, _, _) =>
        val moduleSymbol = enumType.termSymbol.asModule
        mirror.reflectModule(moduleSymbol).instance.asInstanceOf[Enumeration]
    }
  }

  class CaseClassUtil[C <: whitebox.Context, T: C#WeakTypeTag](val c: C) {
    import c.universe._
    val t: Type = weakTypeOf[T]
    if (!t.typeSymbol.isClass || !t.typeSymbol.asClass.isCaseClass) {
      c.error(c.enclosingPosition, s"error: expected case class got $t.")
    }

    lazy val fields: List[Symbol] = t.decls
      .collectFirst {
        case m: MethodSymbol if m.isPrimaryConstructor => m
      }
      .get
      .paramLists
      .head

    lazy val companion: Ident = Ident(TermName(t.typeSymbol.name.decodedName.toString))
  }

}
