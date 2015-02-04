package hu.frankdavid.ranking

case class MatchUp(player1: Player, player2: Player, matchType: Int, enableDraw: Boolean = false) extends
Ordered[MatchUp] {
  override def hashCode() = player1.hashCode + player2.hashCode

  override def equals(obj: scala.Any) = obj match {
    case MatchUp(_first, _second, _matchType, _) =>
      (player1 == _first && player2 == _second || player2 == _first && player1 == _second) &&
        (matchType == _matchType || _matchType == 0 || matchType == 0)
    case _ => false
  }

  def toSeq = Seq(player1, player2)

  def compare(that: MatchUp) = player1.compare(that.player1)
}

object MatchUp {
  def apply(pair: (Player, Player)): MatchUp = MatchUp(pair._1, pair._2, 0)

  def apply(pair: (Player, Player), matchType: Int, enableDraw: Boolean): MatchUp =
    MatchUp(pair._1, pair._2, matchType, enableDraw)

  def apply(pair: Traversable[Player]): MatchUp = MatchUp(pair.head, pair.tail.head)

  def apply(pair: Traversable[Player], matchType: Int, enableDraw: Boolean): MatchUp =
    MatchUp(pair.head, pair.tail.head, matchType, enableDraw)
}
