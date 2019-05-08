package scalest.admin.slick.meta

import scalest.admin.slick.SlickSearchModule
import scalest.meta.{CaseClassUtil, MacroDebug}

import scala.reflect.macros.whitebox

object ModelQueryMacros {
  def impl[Q: c.WeakTypeTag, M: c.WeakTypeTag, T: c.WeakTypeTag](c: whitebox.Context)
                                                                (queryP: c.Tree, excludeP: c.Expr[List[String]]): c.Tree = {
    import c.universe._
    val M = weakTypeOf[M]
    val T = weakTypeOf[T]
    val exclude = c.eval(c.Expr[List[String]](c.untypecheck(excludeP.tree.duplicate)))
    val generated = new CaseClassUtil[c.type, Q](c)
      .fields
      .map(_.asTerm)
      .filter(p => !exclude.contains(p.name.decodedName.toString))
      .map { p =>
        p.typeSignature.typeSymbol match {
          case t if t == weakTypeOf[SlickSearchModule#SearchParam[_]].typeSymbol =>
            q"""(slickQuery: Query[$T, $M, Seq]) => $queryP.${p.name} match {
               case EmptyParam() => slickQuery
               case _ => slickQuery.filter(t => $queryP.${p.name}.interpret(t.${p.name}))
             }"""
          case t if t == definitions.OptionClass                                 =>
            q"(slickQuery: Query[$T, $M, Seq]) => slickQuery.filterOpt($queryP.${p.name})(_.${p.name} === _)"

          case _ =>
            q"(slickQuery: Query[$T, $M, Seq]) => slickQuery.filter(_.${p.name} === $queryP.${p.name})"
        }
      }
      .foldLeft(q"tableQuery": c.Tree)((q, f) => q"$f($q)")

    MacroDebug.logGeneratedCode(c)("MODEL_QUERY_MACROS", generated)
    generated
  }
}