package hu.frankdavid.ranking.gui

import hu.frankdavid.ranking.TournamentStrategy
import hu.frankdavid.ranking.strategy._
import hu.frankdavid.ranking.strategy.util.IntJsFunction

object DefaultStrategies extends Seq[TournamentStrategy] {

  //breeze library may be not available
  val maybeColley = try {
    breeze.storage.Zero
    Some(new ColleyStrategy(IntJsFunction("floor(3 + log(numPlayers) / log(2))")))
  } catch {
    case _: Throwable => None
  }

  private val items = Seq(
    new OneByOneStrategy(GreedyGraphStrategy),
    new RoundRobinStrategy(2),
    MergeStrategy,
    new TimSortStrategy(IntJsFunction("0")),
    new TimSortStrategy(IntJsFunction("numPlayers")),
    new HeapStrategy(IntJsFunction("0")),
    new HeapStrategy(IntJsFunction("numPlayers")),
    GreedyGraphStrategy,
    new MarkovChainStrategy(IntJsFunction("floor(3 + log(numPlayers) / log(2))")),
    new TraditionalSwissStrategy(IntJsFunction("floor(3 + log(numPlayers) / log(2))")),
    FootballWorldCupStrategy
  ) ++ maybeColley


  def length = items.length

  def apply(idx: Int) = items.apply(idx)

  def iterator = items.iterator

}
