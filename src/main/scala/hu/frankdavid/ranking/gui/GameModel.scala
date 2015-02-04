package hu.frankdavid.ranking.gui

import scala.beans.BeanProperty

class GameModel(@BeanProperty val firstName: String,
                @BeanProperty val secondName: String) {
  @BeanProperty var isFirstWinner: Boolean = _
}
