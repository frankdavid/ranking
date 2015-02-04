package hu.frankdavid.ranking.gui

import hu.frankdavid.ranking.TournamentStrategy

import scalafx.geometry.Insets
import scalafx.scene.layout.{Priority, GridPane}

abstract class StrategyConfigurationEditor extends GridPane {
  def load(strategy: TournamentStrategy): Boolean
  def strategy: TournamentStrategy

  hgap = 10
  vgap = 5
  padding = Insets(10)
  hgrow = Priority.Always
}
