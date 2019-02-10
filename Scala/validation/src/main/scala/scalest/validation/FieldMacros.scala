package scalest.validation

import scala.reflect.macros.blackbox

object FieldMacros {

  def childMacro[A: c.WeakTypeTag, B: c.WeakTypeTag](
    c: blackbox.Context,
  )(function: c.Expr[A => B]): c.Expr[Field[B]] = {
    import c.universe._

    //Get body
    val q"($_) => $body" = function.tree

    //Recursively extracts names from call chain
    def extractNames(body: c.Tree): List[String] = body match {
      case Select(rest, name: TermName) => name.toString :: extractNames(q"$rest")
      case Ident(_)                     => List.empty
      case _                            => c.abort(c.enclosingPosition, "Function is not chain of transformations")
    }

    val names = extractNames(q"$body").reverse

    val resultName = names.last

    //Get parent field from prefix
    val q"$_.FieldOps[..$_]($field)" = c.prefix.tree

    //Fold into derived fields structure
    val parents = names
      .dropRight(1)
      .foldRight(field)((n, f) => q"""scalest.validation.Field($n, $f.value.${TermName(n)}, Some($f))""")

    c.Expr[Field[B]](q"""scalest.validation.Field($resultName, $function($field.value), Some($parents))""")
  }

  def fieldMacro[A: c.WeakTypeTag](c: blackbox.Context)(value: c.Expr[A]): c.Tree = {
    import c.universe._
    fieldWithOptionParentMacro(c)(value, q"None")
  }

  def fieldWithParentMacro[A: c.WeakTypeTag](c: blackbox.Context)(value: c.Expr[A], parent: c.Tree): c.Tree = {
    import c.universe._
    fieldWithOptionParentMacro(c)(value, q"Some($parent)")
  }

  def fieldWithOptionParentMacro[A: c.WeakTypeTag](c: blackbox.Context)(value: c.Expr[A], parent: c.Tree): c.Tree = {
    import c.universe._

    val fieldName = value.tree match {
      case Select(_, TermName(propertyName)) => q"$propertyName"
      case Ident(TermName(variableName))     => q"$variableName"
      case _                                 => c.abort(c.enclosingPosition, "Only variables and properties are supported")
    }

    q"""Field($fieldName, $value, $parent)"""
  }
}
