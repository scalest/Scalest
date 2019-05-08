package scalest.tapir

import scalest.meta.CaseClassUtil
import sttp.tapir.EndpointInput

import scala.reflect.macros.whitebox

object TapirCaseQueryMacros {
  def impl[T: c.WeakTypeTag](c: whitebox.Context): c.Expr[EndpointInput[T]] = {
    import c.universe._
    val util = new CaseClassUtil[c.type, T](c)
    val T = weakTypeOf[T]
    val fields = util.fields.map(_.asTerm)
    val tuple = fields
      .zipWithIndex
      .map { case (p, i) =>
        val isOption = p.typeSignature.typeSymbol == typeOf[Option[_]].typeSymbol
        if (!p.isParamWithDefault || isOption) q"_root_.sttp.tapir.query[${p.typeSignature}](${Literal(Constant(p.name.decodedName.toString))})"
        else {
          val default = TermName("apply$default$" + (i + 1))
          q"""_root_.sttp.tapir.query[Option[${p.typeSignature}]](${Literal(Constant(p.name.decodedName.toString))})
             .description("Default: " + ${util.companion}.$default)
             .map(_.getOrElse(${util.companion}.$default))(Some(_))"""
        }
      }
      .reduce((a, b) => q"$a.and($b)")
    val params = fields.map(p => q"c.${TermName(p.name.decodedName.toString)}")
    val tree = q"$tuple.map[$T]((${util.companion}.apply _).tupled)(c => (..$params))"
    c.Expr[EndpointInput[T]](tree)
  }
}
