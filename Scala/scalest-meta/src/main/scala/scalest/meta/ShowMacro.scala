package scalest.meta

import scala.annotation.StaticAnnotation
import scala.collection.immutable
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

class show(exclude: Set[String] = Set.empty) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro ShowMacro.impl
}

object ShowMacro {
  def impl(c: blackbox.Context)(annottees: c.Tree*): c.Tree = {
    import c.universe._

    def abortShowMacro() = c.abort(c.enclosingPosition, "Invalid annotation target: must be a case class")

    def overrideToString(clsDef: ClassDef, clsParams: Seq[Tree]): Tree = {
      val exclude: Set[String] = c.prefix.tree match {
        case q"new show($exclude) " => c.eval(c.Expr[Set[String]](exclude))
        case _                      => abortShowMacro()
      }

      val params = clsParams.map {
        case valDef: ValDef => valDef.name.decodedName.toString
        case _              => abortShowMacro()
      }

      val body: Tree = params
        .filter(!exclude.contains(_))
        .map(fieldName => q"""${Literal(Constant(fieldName))} + " = " + ${TermName(fieldName)}""")
        .reduce((a, b) => q"""$a + ", " + $b""")

      q"""override def toString(): String = ${Literal(Constant(clsDef.name.decodedName.toString))} + "(" + $body + ")" """
    }

    val (clsDef: ClassDef, objDef: immutable.Seq[ModuleDef]) = annottees match {
      case List(clsDef: ClassDef)                    => (clsDef, List.empty)
      case List(clsDef: ClassDef, objDef: ModuleDef) => (clsDef, List(objDef))
      case _                                         => abortShowMacro()
    }

    val tree = clsDef match {
      case q"case class $clsName(..$clsParams) extends { ..$clsEarlyDefs } with ..$clsParents { $clsSelf => ..$clsDefs }" =>
        q"""
        case class $clsName(..$clsParams) extends { ..$clsEarlyDefs } with ..$clsParents { $clsSelf =>
          ..$clsDefs
          ${overrideToString(clsDef, clsParams)}
        }
        ..$objDef
        """

      case _ => abortShowMacro()
    }
    MacroDebug.logGeneratedCode(c)("SHOW MACRO", tree)
    tree
  }
}
