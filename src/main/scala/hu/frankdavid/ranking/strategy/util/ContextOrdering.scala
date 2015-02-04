package hu.frankdavid.ranking.strategy.util

import hu.frankdavid.ranking.strategy.ResultRequiredException
import hu.frankdavid.ranking.{Game, MatchUp, Player, TournamentContext}

import scala.collection.mutable.ListBuffer

class ContextOrdering(maxGuessAhead: Int)(implicit context: TournamentContext) extends Ordering[Player] {

  private val requiredGamesBuffer = new ListBuffer[MatchUp]

  var updatedContext = context

  var resultGraph = context.resultGraph

  def requiresGames: Boolean = requiredGamesBuffer.nonEmpty

  def requiredGames: Set[MatchUp] = requiredGamesBuffer.toSet

  def compare(x: Player, y: Player) = {
    val matchup = MatchUp(x, y)
    resultGraph.compare(x, y) match {
      case 0 => requiredGamesBuffer += matchup
        if (requiredGamesBuffer.size <= maxGuessAhead) {
          guessResult(matchup)
        } else {
          throw ResultRequiredException(requiredGames)
        }
      case other => other
    }
  }

  private def guessResult(matchup: MatchUp): Int = {
    val game = new Game(matchup, context.players.indexOf(matchup.player2), context.players.indexOf(matchup.player1))
    //inverse!
    resultGraph += game
    resultGraph.compare(matchup.player1, matchup.player2)
  }
}
