package hu.frankdavid.ranking

sealed trait MatchesOrResult {
  def chain(other: MatchesOrResult)(combiner: (Seq[Player], Seq[Player]) => MatchesOrResult): MatchesOrResult = {
    (this, other) match {
      case (Matches(m1), Matches(m2)) => Matches(m1 ++ m2)
      case (Matches(m1), Result(_)) => Matches(m1)
      case (Result(_), Matches(m2)) => Matches(m2)
      case (Result(p1), Result(p2)) => combiner(p1, p2)
    }
  }

  def toEither: Either[Set[MatchUp], Seq[Player]]

  def withMaxParallelism(maxParallelism: Int) = this

  def matches: Option[Matches] = None

  def result: Option[Result] = None
}

case class Matches(matchups: Set[MatchUp]) extends MatchesOrResult {
  def toEither = Left(matchups)

  override def withMaxParallelism(maxParallelism: Int) = Matches(matchups.take(maxParallelism))

  override def matches = Some(this)
}

object Matches {
  def apply(pair: (Player, Player), matchType: Int): Matches =  Matches(Set(MatchUp(pair._1, pair._2, matchType)))

  def apply(playerPair: MatchUp): Matches = Matches(Set(playerPair))
}

case class Result(orderedPlayers: Seq[Player]) extends MatchesOrResult {
  def toEither = Right(orderedPlayers)

  override def result = Some(this)
}

object MatchesOrResult {
  def chain(seq: Seq[MatchesOrResult])(combiner: (Seq[Seq[Player]]) => MatchesOrResult): MatchesOrResult = {
    val requiredMatches = seq.collect {
      case Matches(pp) => pp
    }.flatten.toSet
    if (requiredMatches.nonEmpty) {
      Matches(requiredMatches)
    } else {
      val results = seq.collect {
        case Result(r) => r
      }
      combiner(results)
    }
  }
}
