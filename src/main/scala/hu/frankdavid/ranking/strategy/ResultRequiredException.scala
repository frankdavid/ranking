package hu.frankdavid.ranking.strategy

import hu.frankdavid.ranking.MatchUp

case class ResultRequiredException(requiredGames: Set[MatchUp]) extends Exception
