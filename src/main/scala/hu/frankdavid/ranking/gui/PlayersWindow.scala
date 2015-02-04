package hu.frankdavid.ranking.gui


import java.io._

import hu.frankdavid.ranking.Player
import hu.frankdavid.ranking.gui.util.ConverterListCell
import org.controlsfx.dialog.Dialogs

import scala.io.Source
import scalafx.Includes._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Button, ListView}
import scalafx.scene.layout.{HBox, Priority, Region, VBox}
import scalafx.stage.FileChooser.ExtensionFilter
import scalafx.stage.{FileChooser, Stage}

class PlayersWindow(val stage: Stage) extends Scene {
  val playersListView = new ListView[Player]() {
    hgrow = Priority.Always
    vgrow = Priority.Always
    cellFactory = _ => new ConverterListCell[Player](_.name)
  }

  root =
    new VBox() {
      hgrow = Priority.Always
      alignment = Pos.CenterRight
      content = Seq(
        new HBox() {
          spacing = 10
          padding = Insets(10)
          hgrow = Priority.Always
          content = Seq(
            new Button("Add") {onAction = add _},
            new Button("Remove") {onAction = remove _},
            new Region {hgrow = Priority.Always},
            new Button("Load from file") {onAction = load _},
            new Button("Save to file") {onAction = save _}
          )
        },
        playersListView,
        new HBox {
          margin = Insets(10)
          content = Seq(
            new Button("Charts") {onAction = {() => stage.setOpacity(0); stage.scene = new ChartScene(stage)}},
            new Region {hgrow = Priority.Always},
            new Button("Next") {
              onAction = next _
              disable = true
              playersListView.items().onChange {disable = playersListView.items().isEmpty}
            }
          )
        }
      )
    }

  def next(): Unit = {
    stage.scene = new TournamentScene(playersListView.items(), stage)
  }

  def add(): Unit = {
    val playerName = Dialogs.create().title("Name of the contestant").message("Name of the contestant").showTextInput()
    if (playerName.isPresent) {
      playersListView.items() += Player(playerName.get)
    }

  }

  def remove(): Unit = {
    val selection = playersListView.selectionModel().getSelectedItem
    if (selection != null) {
      playersListView.items() -= selection
    }
  }

  val fileChooser = new FileChooser() {
    selectedExtensionFilter = new ExtensionFilter("Text files", "*.txt")
  }

  def load(): Unit = {
    val file = fileChooser.showOpenDialog(window())
    if (file != null) {
      playersListView.items().clear()
      playersListView.items() ++= readPlayersFromFile(file)
    }
  }

  def readPlayersFromFile(file: File): Seq[Player] = {
    val source = Source.fromFile(file)
    val players = (for (name <- source.getLines() if !name.isEmpty) yield Player(name)).toBuffer
    source.close()
    players
  }

  def save(): Unit = {
    val file = fileChooser.showSaveDialog(window())
    if (file != null) {
      writePlayersToFile(playersListView.items(), file)
      Dialogs.create().title("Success").message(s"Successfully saved to ${file.getName}.").showInformation()
    }
  }

  def writePlayersToFile(players: Traversable[Player], file: File): Unit = {
    val writer = new PrintWriter(new FileOutputStream(file))
    for(player <- players) {
      writer.println(player.name)
    }
    writer.close()
  }
}
