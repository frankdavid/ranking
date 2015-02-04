package hu.frankdavid.ranking.strategy.util

import hu.frankdavid.ranking.util.SetMultiMap
import hu.frankdavid.ranking.{Game, GameHistory, Player}

case class ResultGraph(worsePlayers: SetMultiMap[Player, Player], betterPlayers: SetMultiMap[Player, Player])
  extends Ordering[Player] {

  def ++(games: Iterable[Game]): ResultGraph = {
    games.foldLeft(this)(_ + _)
  }

  def sortDescending(player1: Player, player2: Player): Option[Seq[Player]] = {
    compare(player1, player2) match {
      case 1 => Some(Seq(player1, player2))
      case -1 => Some(Seq(player2, player1))
      case 0 => None
    }
  }


  def withGame(game: Game): ResultGraph = {
    val maybeNew = for (winner <- game.winner; loser <- game.loser) yield {
      var newWorsePlayers = worsePlayers &(winner, loser)
      var newBetterPlayers = betterPlayers &(loser, winner)


      betterPlayers(winner).foreach { winner =>
        newWorsePlayers &=(winner, loser)
        newBetterPlayers &=(loser, winner)
      }

      worsePlayers(loser).foreach { loser =>
        newWorsePlayers &=(winner, loser)
        newBetterPlayers &=(loser, winner)
      }
      new ResultGraph(newWorsePlayers, newBetterPlayers)
    }
    maybeNew.getOrElse(this)
  }

  def +(game: Game): ResultGraph = withGame(game)

  def compare(x: Player, y: Player) =
    if (worsePlayers(x).contains(y)) 1
    else if (worsePlayers(y).contains(x)) -1
    else 0
}


object ResultGraph {
  val empty: ResultGraph = {
    new ResultGraph(SetMultiMap.empty, SetMultiMap.empty)
  }

  def apply(gameHistory: GameHistory): ResultGraph = {
    gameHistory.allGames.foldLeft(empty) {_ + _}
  }
}
