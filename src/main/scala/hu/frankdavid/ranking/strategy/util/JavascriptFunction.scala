package hu.frankdavid.ranking.strategy.util

import javax.script.{ScriptEngine, ScriptEngineManager, SimpleBindings}

import hu.frankdavid.ranking.TournamentContext
import hu.frankdavid.ranking.strategy.StrategyException

import scala.collection.JavaConversions._
import scala.collection.mutable

abstract class JavascriptFunction[R](script: String) extends (TournamentContext => R) {

  def apply(context: TournamentContext) = {
    val functions = JavascriptFunction.functions.mkString("|")
    val preparedScript = script.replaceAll("(" + functions + """)\(""", "Math.$1(")
    val bindings = mutable.Map(
      "maxParallelism" -> context.maxParallelism,
      "numPlayers" -> context.players.size,
      "numAwardedPlayers" -> context.numAwardedPlayers
    ).asInstanceOf[mutable.Map[String, AnyRef]]
    try {
      val result = JavascriptFunction.engine.eval(preparedScript, new SimpleBindings(bindings))
      cast(result)
    } catch {
      case _: Throwable =>
        throw new StrategyException(s"Encountered an error while executing the following script:\n$script")
    }
  }

  def cast(value: AnyRef): R

  override def toString = script
}

case class IntJsFunction(script: String) extends JavascriptFunction[Int](script) {
  def cast(value: AnyRef) = value.asInstanceOf[Number].intValue()

  override def toString = script
}

case class DoubleJsFunction(script: String) extends JavascriptFunction[Double](script) {
  def cast(value: AnyRef) = value.asInstanceOf[Number].doubleValue()

  override def toString = script
}

case class StringJsFunction(script: String) extends JavascriptFunction[String](script) {
  def cast(value: AnyRef) = value.toString

  override def toString = script
}

object JavascriptFunction {
  val functions: Seq[String] = Seq("sin", "log", "cos", "exp", "floor", "ceil", "round")

  private val engine: ScriptEngine = new ScriptEngineManager().getEngineByName("JavaScript")
}
