package hu.frankdavid.ranking.gui

import javafx.scene.control.Alert.AlertType
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.control.{Alert, ButtonType}

import hu.frankdavid.ranking._
import hu.frankdavid.ranking.strategy._
import hu.frankdavid.ranking.util.Optional2Option._
import myjavafx.spinner.NumberSpinner
import org.controlsfx.dialog.Dialogs

import scala.util.Random
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.beans.property.ReadOnlyStringWrapper
import scalafx.event.ActionEvent
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.TableColumn._
import scalafx.scene.control._
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout._
import scalafx.scene.text.Font
import scalafx.stage.{Screen, Stage}

class TournamentScene(players: Seq[Player], stage: Stage) extends Scene {

  var round = 0

  Platform.runLater {
    window().setWidth(Screen.primary.bounds.width * 0.9)
    window().centerOnScreen()
  }

  val resultLabel = new Label("Select a strategy on the left to begin") {
    font() = Font(20)
  }

  val historyTable = new TableView[Game] {
    placeholder = new Label("The match history is empty")
    vgrow = Priority.Always
    hgrow = Priority.Always
    val outer: TableView[Game] = this
    columns ++= Seq(
      new TableColumn[Game, String] {
        text = "Player 1"
        prefWidth <== outer.width * 0.24
        cellValueFactory = c => ReadOnlyStringWrapper(c.value.first.name)
      },
      new TableColumn[Game, String] {
        text = "Player 2"
        prefWidth <== outer.width * 0.24
        cellValueFactory = c => ReadOnlyStringWrapper(c.value.second.name)
      },
      new TableColumn[Game, String] {
        text = "Winner"
        prefWidth <== outer.width * 0.5
        cellValueFactory = c => {
          val resultAsText = c.value.winner match {
            case Some(player) => "Winner is " + player.name
            case _ => "Draw"
          }
          ReadOnlyStringWrapper(s"$resultAsText (${c.value.score1.toInt} - ${c.value.score2.toInt})")
        }
      })
  }

  val immediateModeCheckBox = new CheckBox()

  val requiredMatchesTable = new TableView[MatchUp] {
    vgrow = Priority.Always
    placeholder = resultLabel
    minWidth = 400
    rowFactory = _ => {
      new TableRow[MatchUp] {
        onMouseClicked = { clickEvent: MouseEvent =>
          if (clickEvent.clickCount > 1) {
            val maybeGame = if (item() == null) {
              inputCustomGame()
            } else {
              inputGame(item())
            }
            for (game <- maybeGame) {
              historyTable.items() += game
              items() -= item()
              if (immediateModeCheckBox.selected() || items().size() == 0) {
                round += 1
                // immediate or no more pairs left
                refreshRequiredMatches()
              }
            }

          }
        }
      }
    }
    val outer = this
    columns ++= Seq(
      new TableColumn[MatchUp, String] {
        prefWidth <== outer.width * 0.48
        text = "Player 1"
        cellValueFactory = c => ReadOnlyStringWrapper(c.value.player1.name)
      },
      new TableColumn[MatchUp, String] {
        prefWidth <== outer.width * 0.48
        text = "Player 2"
        cellValueFactory = c => ReadOnlyStringWrapper(c.value.player2.name)
      }
    )
  }

  val awardedSpinner = new NumberSpinner(1, players.size max 1)
  awardedSpinner.setValue(3)
  awardedSpinner.onAction = refreshRequiredMatches _

  val parallelMatchesSpinner = new NumberSpinner(1, players.size / 2 max 1)
  parallelMatchesSpinner.setValue(players.size / 2)
  parallelMatchesSpinner.onAction = refreshRequiredMatches _


  var selectedStrategy: TournamentStrategy = _
  var strategyLabel = new Label("Nothing selected") {
    alignment = Pos.BottomLeft
    padding = Insets(0, 10, 0, 0)
  }
  root =
    new HBox {
      hgrow = Priority.Always
      vgrow = Priority.Always
      content = historyTable
      val outer = this
      content = Seq(
        new GridPane {
          alignment = Pos.CenterLeft
          padding = Insets(10)
          prefWidth <== outer.width * 0.3
          hgap = 10
          vgap = 10
          add(new Label("Strategy: "), 0, 0)
          add(new HBox {
            content = Seq(
              strategyLabel,
              new Button("Edit") {
                minWidth = 60
                onAction = { _: ActionEvent =>
                  new StrategyDialog(selectedStrategy).showAndWait().foreach { strategy =>
                    selectedStrategy = strategy
                    strategyLabel.text = strategy.toString
                    refreshRequiredMatches()
                  }
                }
              }
            )
          }, 1, 0)
          add(new Label("Awarded players: "), 0, 1)
          add(jfxControl2sfx(awardedSpinner), 1, 1)
          add(new Label("Parallel matches: "), 0, 2)
          add(jfxControl2sfx(parallelMatchesSpinner), 1, 2)
          add(new Label("Immediate mode: "), 0, 3)
          add(immediateModeCheckBox, 1, 3)
        },
        new VBox() {
          alignment = Pos.Center
          vgrow = Priority.Always
          spacing = 0
          prefWidth <== outer.width * 0.2
          content = requiredMatchesTable
        },
        historyTable
      )
    }

  def gameHistory: GameHistory = {
    GameHistory(historyTable.items().toList)
  }

  def refreshRequiredMatches(): Unit = {
    if (selectedStrategy != null) {
      val tournamentContext = new TournamentContext(
        players = players,
        numAwardedPlayers = awardedSpinner.getValue.intValue(),
        randomSeed = Random.nextLong(),
        maxParallelism = parallelMatchesSpinner.getValue.intValue(),
        round = round,
        gameHistory = gameHistory)
      try {
        val matchesOrResult = selectedStrategy.matchesOrResult(tournamentContext)

        matchesOrResult match {
          case Matches(requiredMatches) => {
            requiredMatchesTable.items().clear()
            requiredMatchesTable.items() ++= requiredMatches
          }
          case Result(sortedPlayers) => {
            displaySortedPlayers(sortedPlayers)
          }
        }
      } catch {
        case StrategyException(message) => handleException(message)
        case e: Throwable => Dialogs.create().showException(e)
      }
    }
  }


  private def handleException(message: String) {
    Platform.runLater {
      val alert = new Alert(AlertType.WARNING,
        s"Could not execute simulation of $selectedStrategy because of the following error:\n$message",
        new ButtonType("OK", ButtonData.OK_DONE))
      alert.show()
      alert.setHeight(300)
    }
  }

  def displaySortedPlayers(players: Seq[Player]): Unit = {
    val label = (for ((player, i) <- players.zipWithIndex) yield s"${i + 1}. ${player.name}").mkString("\n")
    requiredMatchesTable.items().clear()
    resultLabel.text() = label
  }

  def inputCustomGame(): Option[Game] = {
    val p1Name = Dialogs.create().showTextInput()
    if (p1Name.isPresent) {
      val p1 = players.find(_.name == p1Name.get())
      val p2Name = Dialogs.create().showTextInput()
      if (p2Name.isPresent) {
        val p2 = players.find(_.name == p2Name.get())
        return inputGame(MatchUp(p1.get, p2.get))
      }
    }
    None
  }

  def inputGame(matchup: MatchUp, defaultScore1: Int = 0, defaultScore2: Int = 0): Option[Game] = {
    val title = if (matchup.enableDraw)  "Please enter the result"
                else "Please enter the result (draw is not possible)"
    val result = Dialogs.create()
                  .title(title)
                  .message(s"${matchup.player1.name} - ${matchup.player2.name}")
                  .showTextInput(s"$defaultScore1-$defaultScore2")
    
    if (result.isPresent) {
      val resultRegex = """\D*(\d+)\D+(\d+)\D*""".r
      result.get() match {
        case resultRegex(score1, score2) =>
          val score1Int = score1.toInt
          val score2Int = score2.toInt
          if (!matchup.enableDraw && score1Int == score2Int) {
            new Alert(AlertType.WARNING, "Draw is not enabled on this match",
              new ButtonType("OK", ButtonData.OK_DONE)).showAndWait()
            inputGame(matchup, score1Int, score2Int)
          } else {
            Some(Game(matchup, score1Int, score2Int))
          }
        case _ => None
      }
    } else {
      None
    }
  }


}
