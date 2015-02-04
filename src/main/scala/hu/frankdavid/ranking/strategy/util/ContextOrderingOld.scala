package hu.frankdavid.ranking.strategy.util

import hu.frankdavid.ranking.strategy.ResultRequiredException
import hu.frankdavid.ranking.{MatchUp, Player, TournamentContext}

import scala.collection.mutable.ListBuffer

class ContextOrderingOld(maxGuessAhead: Int)(implicit context: TournamentContext) extends Ordering[Player] {

  private val requiredGamesBuffer = new ListBuffer[MatchUp]

  def requiresGames: Boolean = requiredGamesBuffer.nonEmpty

  def requiredGames: Set[MatchUp] = requiredGamesBuffer.toSet

  def compare(x: Player, y: Player) = {
    val pair = MatchUp(x, y)
    context.gameHistory.maybeLatestGame(pair) match {
      case Some(game) => game.compare(x, y)
      case None => {
        requiredGamesBuffer += pair
        if (requiredGamesBuffer.size <= maxGuessAhead) {
          guessResult(x, y)
        } else {
          throw ResultRequiredException(requiredGames)
        }
      }
    }
  }

  private def guessResult(x: Player, y: Player): Int = {
    context.players.indexOf(y).compareTo(context.players.indexOf(x))
  }
}
