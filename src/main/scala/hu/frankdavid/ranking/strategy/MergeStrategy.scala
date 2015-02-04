package hu.frankdavid.ranking.strategy

import hu.frankdavid.ranking._

import scala.annotation.tailrec
import scala.collection.immutable.Queue

/**
 * This strategy does a bounded merge sort.
 */
case object MergeStrategy extends TournamentStrategy("Merge strategy") {

  override def matchesOrResult(implicit context: TournamentContext): MatchesOrResult = {
    new Group(context.players).matchesOrResult
  }

  private class Group(players: Seq[Player])(implicit context: TournamentContext) {

    private lazy val subGroups = players.grouped((players.size + 1) / 2).map(new Group(_)).toList
    private lazy val matchOfTwo = {
      require(players.size == 2)
      MatchUp(players.head, players.tail.head)
    }

    lazy val matchesOrResult: MatchesOrResult = {
      if (players.size == 1) {
        Result(Seq(players.head))
      } else if (players.size == 2) {
        resultForPair
      } else {
        resultForManyPlayers
      }
    }

    def resultForManyPlayers: MatchesOrResult = {
      MatchesOrResult.chain(subGroups.map(_.matchesOrResult)) { (results: Seq[Seq[Player]]) =>
        @tailrec def merge(left: Seq[Player], right: Seq[Player], resultAcc: Queue[Player]): MatchesOrResult = {
          if (resultAcc.size >= context.numAwardedPlayers) {
            Result(resultAcc)
          } else if (left.nonEmpty && right.nonEmpty) {
            val m = MatchUp(left.head, right.head)
            context.resultGraph.compare(left.head, right.head) match {
              case 1 =>
                merge(left.tail, right, resultAcc.enqueue(left.head))
              case -1 =>
                merge(left, right.tail, resultAcc.enqueue(right.head))
              case 0 => Matches(m)
            }
          } else if (left.nonEmpty) {
            merge(left.tail, right, resultAcc.enqueue(left.head))
          } else if (right.nonEmpty) {
            merge(left, right.tail, resultAcc.enqueue(right.head))
          } else {
            Result(resultAcc)
          }
        }
        merge(results(0), results(1), Queue())
      }
    }

    def resultForPair: MatchesOrResult = {
      context.resultGraph.sortDescending(players.head, players(1)).map { sorted =>
        Result(sorted)
      }.getOrElse(Matches(matchOfTwo))
    }
  }
}
