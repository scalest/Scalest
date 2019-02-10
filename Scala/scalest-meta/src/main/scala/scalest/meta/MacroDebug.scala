package scalest.meta

import scala.reflect.macros.blackbox

object MacroDebug {

  private val macroDebugEnabled: Boolean = true
  System.getenv("LOG_GENERATED_CODE") == "true"

  def logGeneratedCode(c: blackbox.Context)(label: String, tree: c.universe.Tree): Unit = {
    import c.universe._
    if (macroDebugEnabled) {
      println(s"""$label macro output start:""")
      println(showCode(tree))
      println(s"""$label macro output end.""")
    }
  }
}
