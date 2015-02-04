package hu.frankdavid.ranking.gui.configs

import hu.frankdavid.ranking.strategy.util.JavascriptFunction

import scalafx.scene.control.Label

class JSEditorLabel extends Label {
  text() =
    s"""
       |You can use mathematical expressions.
       |The following variables are available:
       |maxParallelism, numPlayers, numAwardedPlayers
       |You can also use the following functions:
       |${JavascriptFunction.functions.mkString(", ")}
    """.stripMargin
}
