package hu.frankdavid.ranking.gui

import java.awt.Toolkit

import com.apple.eawt.Application

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.image.Image
import scalafx.Includes._

object Main extends JFXApp {

  val icon = getClass.getResource("icon.png")
  try {
    Application.getApplication.setDockIconImage(Toolkit.getDefaultToolkit.getImage(icon))
  } catch {
    case _: Throwable =>
  }
  stage = new PrimaryStage() {
    title = "Tournament Organizer"
    icons += new Image(icon.openStream())
  }
  stage.scene = new PlayersWindow(stage)
}

