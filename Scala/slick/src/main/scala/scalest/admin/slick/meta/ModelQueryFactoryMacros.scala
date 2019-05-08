package scalest.admin.slick.meta

import scalest.meta.MacroDebug

import scala.reflect.macros.blackbox

object ModelQueryFactoryMacros {
  def impl[Q: c.WeakTypeTag](c: blackbox.Context)
                            (hintP: c.Tree): c.Tree = {
    import c.universe._
    val Q = weakTypeOf[Q]

    val q"($_) => $body" = hintP

    def hint = body match {
      case Select(_, TermName(name)) => name
      case _                         => c.abort(c.enclosingPosition, "Function is not exactly one field")
    }

    val companion = Ident(TermName(weakTypeOf[Q].typeSymbol.name.decodedName.toString))
    val id = TermName(hint)
    val generated =
      q"""new ModelQueryFactory[$Q]{
           def fromId(id: $Q#Id): $Q = $companion($id = eqs(id))
           def fromIds(ids: Seq[$Q#Id]): $Q = $companion($id = inSet(ids))
           def empty: $Q = $companion()
         }"""

    MacroDebug.logGeneratedCode(c)("ENTITY_QUERY_FACTORY_MACROS", generated)
    generated
  }
}
