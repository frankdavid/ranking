package hu.frankdavid.pingpong

case class Match(player1: Player, player2: Player) {
  def swap: Match = Match(player2, player1)
}
