package hu.frankdavid.ranking.strategy

import hu.frankdavid.ranking.util.RingBuffer
import hu.frankdavid.ranking._

import scala.collection.mutable

case class RoundRobinStrategy(rounds: Int = 1, win: Int = 3, draw: Int = 1, lose: Int = 0) extends TournamentStrategy
("Round Robin Strategy") {
  def matchesOrResult(implicit context: TournamentContext) = {
    val games = requiredGames
    if (games.isEmpty) {
      val results = new mutable.HashMap[Player, Int]().withDefaultValue(0)
      context.gameHistory.allGames.foreach { game =>
        game.winnerLoser match {
          case Some((winner, loser)) =>
            results(winner) = results(winner) + win
            results(loser) = results(loser) + lose
          case _ =>
            results(game.first) = results(game.first) + draw
            results(game.second) = results(game.second) + draw
        }
      }
      Result(context.players.sortBy(player => -results(player)).take(context.numAwardedPlayers))
    } else {
      Matches(games.take(context.maxParallelism))
    }
  }

  private def requiredGames(implicit context: TournamentContext): Set[MatchUp] = {
    val games = new mutable.SetBuilder[MatchUp, Set[MatchUp]](Set())
    val playersSeq = context.players.toVector
    val evenPlayersSeq =
      if (playersSeq.size % 2 == 1) null +: playersSeq
      else playersSeq
    val baseTailRing = new RingBuffer[Player](evenPlayersSeq.tail)
    var tailRing = baseTailRing
    val busy = new mutable.HashSet[Player]()
    do {
      val players = Vector(evenPlayersSeq.head) ++ tailRing
      val pairs = generatePairs(players)
      for (pair <- pairs)
        if (!busy.contains(pair.player1) && !busy.contains(pair.player2) && context.gameHistory(pair).size < rounds) {
          games += pair
          busy ++= pair.toSeq
        }
      tailRing = tailRing.shiftRight
    } while (tailRing != baseTailRing)
    games.result()
  }

  private def generatePairs(players: Seq[Player]) = {
    require(players.size % 2 == 0, "Size of players must be even")
    for (firstIndex <- 0 until players.size / 2;
         secondIndex = players.size - firstIndex - 1;
         first = players(firstIndex);
         second = players(secondIndex)
         if first != null && second != null && first != second
    ) yield MatchUp(first, second)
  }
}
