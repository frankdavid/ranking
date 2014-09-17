package hu.frankdavid.pingpong.strategy

import hu.frankdavid.pingpong.{Match, Player, TournamentContext}

import scala.annotation.tailrec
import scala.collection.immutable.Queue

class MergeMatchOrganizerStrategy extends MatchOrganizerStrategy("Merge strategy") {

  override def matchesOrResult(implicit context: TournamentContext): Either[Set[Match], Seq[Player]] =
    new Group(context.players).matchesOrResult

  private class Group(players: Set[Player])(implicit context: TournamentContext) {

    private lazy val subGroups = players.grouped((players.size + 1) / 2).map(new Group(_)).toList
    private lazy val matchOfTwo = {
      require(players.size == 2)
      Match(players.head, players.tail.head)
    }

    lazy val matchesOrResult: Either[Set[Match], Seq[Player]] = {
      if (players.size == 1) {
        Right(Seq(players.head))
      } else if (players.size == 2) {
        context.resultOption(matchOfTwo).map { res =>
          val playerSeq = if (res > 0) players.toSeq else players.toSeq.reverse
          Right(playerSeq)
        }.getOrElse(Left(Set(matchOfTwo)))
      } else {
        val subMatches = subGroups.flatMap(_.matchesOrResult.left.toOption)
        if (subMatches.nonEmpty) {
          Left(subMatches.flatten.toSet)
        } else {
          @tailrec def merge(left: Seq[Player], right: Seq[Player], accumulator: Queue[Player]):
          Either[Set[Match], Seq[Player]] = {
            if (accumulator.size >= context.topN) {
              Right(accumulator)
            } else if (left.nonEmpty && right.nonEmpty) {
              val m = Match(left.head, right.head)
              context.resultOption(m) match {
                case Some(res) =>
                  if (res > 0) {
                    merge(left.tail, right, accumulator.enqueue(left.head))
                  } else {
                    merge(left, right.tail, accumulator.enqueue(right.head))
                  }
                case _ => Left(Set(m))
              }
            } else if (left.nonEmpty) {
              merge(left.tail, right, accumulator.enqueue(left.head))
            } else if (right.nonEmpty) {
              merge(left, right.tail, accumulator.enqueue(right.head))
            } else {
              Right(accumulator)
            }
          }
          merge(subGroups(0).matchesOrResult.right.get, subGroups(1).matchesOrResult.right.get, Queue())
        }
      }
    }
  }

  //    def result(exception: Boolean): Seq[Player] = {
  //      if (players.size == 1) {
  //        List(players.head)
  //      } else if (players.size == 2) {
  //        context.resultOption(matchOfTwo) match {
  //          case Some(res) =>
  //            if (res > 0)
  //              players.toList
  //            else
  //              players.toList.reverse
  //          case _ if exception => throw new ResultRequiredException(matchOfTwo)
  //          case _ => List()
  //        }
  //      } else {
  //        @tailrec def merge(left: Seq[Player], right: Seq[Player], accumulator: Queue[Player]): Seq[Player] = {
  //          if (accumulator.size >= context.topN) {
  //            accumulator
  //          } else if (left.nonEmpty && right.nonEmpty) {
  //            val m = Match(left.head, right.head)
  //            context.resultOption(m) match {
  //              case Some(res) =>
  //                if (res > 0) {
  //                  merge(left.tail, right, accumulator.enqueue(left.head))
  //                } else {
  //                  merge(left, right.tail, accumulator.enqueue(right.head))
  //                }
  //              case None if exception => throw new ResultRequiredException(m)
  //              case _ => (accumulator ++ left ++ right).take(context.topN)
  //            }
  //          } else if (left.nonEmpty) {
  //            merge(left.tail, right, accumulator.enqueue(left.head))
  //          } else if (right.nonEmpty) {
  //            merge(left, right.tail, accumulator.enqueue(right.head))
  //          } else {
  //            accumulator
  //          }
  //        }
  //        merge(subGroups(0).result(false), subGroups(1).result(false), Queue())
  //      }
  //    }
  //  }
}