package hu.frankdavid.ranking.gui

import java.util.concurrent.atomic.AtomicInteger
import javafx.beans.property.SimpleListProperty
import javafx.concurrent.Task
import javafx.scene.Group
import javafx.scene.chart.XYChart
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.control.{Alert, ButtonType}
import javafx.scene.text.Text

import hu.frankdavid.ranking.TournamentStrategy
import hu.frankdavid.ranking.strategy._
import hu.frankdavid.ranking.workbench.{SingleTestResult, TestResultLike, TestRunner}
import myjavafx.spinner.NumberSpinner
import org.controlsfx.dialog.Dialogs

import scalafx.Includes._
import scalafx.application.Platform
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.chart.XYChart.{Data, Series}
import scalafx.scene.chart.{BarChart, CategoryAxis, NumberAxis}
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.stage.{Screen, Stage}

class ChartScene(stage: Stage) extends Scene {

  val results = new SimpleListProperty[(TournamentStrategy, TestResultLike)](
    ObservableBuffer.empty[(TournamentStrategy, TestResultLike)])

  val strategyPicker = new StrategyPicker()

  val numberOfPlayersSpinner = new NumberSpinner()
  numberOfPlayersSpinner.setMinValue(1)
  numberOfPlayersSpinner.setValue(16)

  Platform.runLater {
    window().width = Screen.primary.bounds.width
    window().height = Screen.primary.bounds.height
    window().centerOnScreen()
    window().opacity = 1
  }

  val resultPredictionInaccuracySlider = new Slider(1e-10, 1, 0) {
    blockIncrement = 0.1
    majorTickUnit = 0.1
    showTickLabels = true
    hgrow = Priority.Always
  }

  val maxParallelismSlider = new Slider(0, 1, 1) {
    blockIncrement = 0.1
    majorTickUnit = 0.1
    showTickLabels = true
    hgrow = Priority.Always
  }

  val playerPerformanceDeviationSlider = new Slider(1e-10, 1, 0) {
    blockIncrement = 0.1
    majorTickUnit = 0.1
    showTickLabels = true
    hgrow = Priority.Always
  }

  val topNSpinner = new NumberSpinner()
  topNSpinner.setMinValue(1)
  topNSpinner.setValue(3)
  topNSpinner.maxValueProperty() <== numberOfPlayersSpinner.valueProperty()

  val testCasesSpinner = new NumberSpinner()
  testCasesSpinner.setMinValue(1)
  testCasesSpinner.setValue(1)

  val selectedStrategyResult = new ObjectProperty[TestResultLike](this, "selectedStrategyResult")

  root = new SplitPane {
    prefWidth = Region.USE_COMPUTED_SIZE
    padding = Insets(10)
    dividerPositions = 0.3
    items ++= Seq(
      new VBox {
        padding = Insets(10)
        spacing = 10
        alignment = Pos.CenterRight
        content = Seq(new GridPane {
          spacing = 10
          vgap = 10
          hgap = 5
          add(new Label("Number of test cases: "), 0, 0)
          add(jfxControl2sfx(testCasesSpinner), 1, 0)
          add(new Label("Number of players: "), 0, 1)
          add(jfxControl2sfx(numberOfPlayersSpinner), 1, 1)
          add(new Label("Performance deviation: "), 0, 2)
          add(playerPerformanceDeviationSlider, 1, 2)
          add(new Label("Result prediction inaccuracy: "), 0, 3)
          add(resultPredictionInaccuracySlider, 1, 3)
          add(new Label("Awarded players: "), 0, 4)
          add(jfxControl2sfx(topNSpinner), 1, 4)
          add(new Label("Maximum parallelism: "), 0, 5)
          add(maxParallelismSlider, 1, 5)
          add(strategyPicker, 0, 6, 2, 1)
        },
          new Button("Organize matches") {onAction = simulate _; hgrow = Priority.Always}
        )
      }.delegate,
      new ScrollPane {
        maxHeight = 800
        prefWidth = Screen.primary.bounds.width
        content =
          new GridPane() {
            hgrow = Priority.Always
            add(new MatchStatisticsBarChart(
              "Max number of matches per player", "Number of matches", _.maxNumberOfGamesPerPlayer), 0, 0)
            add(new MatchStatisticsBarChart("Number of matches", "Number of matches", _.numberOfGames), 1, 0)
            add(new MatchStatisticsBarChart("Number of rounds", "Number of rounds", _.numberOfRounds), 0, 1)
            add(NumberOfGamesPerPlayerBarChart, 1, 1)
            add(new MatchStatisticsBarChart(
              "Result difference (lower is better)", "Result squared distance", _.resultDistance), 0, 2)
            add(new MatchStatisticsBarChart("Silver is correct", "Number of matches", _.placeGuessedCorrectly(1)), 0, 3)
          }
      }.delegate
    )

  }
  stylesheets += getClass.getResource("common.css").toExternalForm


  def simulate(): Unit = {
    val numberOfPlayers = numberOfPlayersSpinner.getValue.intValue()
    val testRunner = new TestRunner(
      numberOfPlayers = numberOfPlayers,
      awardedPlayers = topNSpinner.getValue.intValue(),
      playerPerformanceDeviation = playerPerformanceDeviationSlider.value(),
      resultPredictionDeviation = resultPredictionInaccuracySlider.value(),
      maxParallelism = (maxParallelismSlider.value() * numberOfPlayers / 2).toInt max 1
    )

    val task = new Task[Unit] {
      def call() = {
        val progress = new AtomicInteger()
        val strategies = strategyPicker.strategies
        val newResults = strategies.par.flatMap { strategy =>
          try {
            val runResult = testRunner.runMany(strategy, testCasesSpinner.getValue.intValue())
            Some(strategy -> runResult)
          } catch {
            case StrategyException(message) =>
              handleException(strategy, message)
              None
            case e: Throwable => e.printStackTrace(); None
          } finally {
            updateProgress(progress.incrementAndGet(), strategies.size())
          }
        }.seq
        if (!isCancelled) {
          Platform.runLater {
            results.clear()
            results ++= newResults
            stage.centerOnScreen()
            window().opacity = 1
          }
        }
      }
    }
    new Thread(task).start()
    Dialogs.create().showWorkerProgress(task)
  }

  private def handleException(strategy: TournamentStrategy, message: String) {
    Platform.runLater {
      val alert = new Alert(AlertType.WARNING,
        s"Could not execute simulation of $strategy because of the following error:\n$message",
        new ButtonType("OK", ButtonData.OK_DONE))
      alert.showAndWait()
      alert.setHeight(300)
    }
  }

  private def displayLabelForData(data: XYChart.Data[String, Number]) {
    val node = data.getNode
    val dataString = data.getYValue match {
      case x: java.lang.Double => x.formatted("%.2f")
      case x: java.lang.Float => x.formatted("%.2f")
      case o => o.toString
    }
    val dataText = new Text(dataString)
    node.parentProperty().onChange { (_, old, parent) =>
      if (old != null) {
        val oldParentGroup = old.asInstanceOf[Group]
        oldParentGroup.getChildren.collect {
          case t: Text => Platform.runLater(oldParentGroup.getChildren.remove(t))
        }
      }
      if (parent != null) {
        val parentGroup = parent.asInstanceOf[Group]
        parentGroup.children += dataText
      }
    }
    node.boundsInParentProperty().onChange { (_, _, bounds) =>
      dataText.layoutX = math.round(bounds.getMinX + bounds.getWidth / 2 - dataText.prefWidth(-1) / 2)
      val minY = math.round(bounds.getMaxY - dataText.prefHeight(-1))
      val Y = math.round(bounds.getMinY + dataText.prefHeight(-1))
      dataText.layoutY = math.min(minY, Y)
    }
  }

  private class MatchStatisticsBarChart(chartTitle: String, categoryTitle: String,
                                        attribute: TestResultLike => Number) extends Pane {
    observableList2ObservableBuffer(results).onChange { (newResults, _) =>
      content = new BarChart(CategoryAxis("Strategy"), NumberAxis(categoryTitle)) {
        prefHeight = 300
        title = chartTitle
        legendVisible = false
        val seriesBuffer = ObservableBuffer[XYChart.Data[String, Number]]()
        seriesBuffer ++= newResults.zipWithIndex.map {
          case ((strategy, result), index) => {
            val label = (index + 1) + ". " + strategy.typeName.replaceAll("(?i)strategy", "")
            val data = Data[String, Number](label, attribute(result))
            data.nodeProperty().onChange { (_, _, node) =>
              displayLabelForData(data)
              node.onMouseEntered = { () => selectedStrategyResult() = result}
            }
            data
          }
        }
        data() = ObservableBuffer(Series(seriesBuffer))
      }
    }
  }

  private object NumberOfGamesPerPlayerBarChart extends Pane {
    selectedStrategyResult.onChange { (_, _, strategyResult) =>
      content = strategyResult match {
        case singleResult: SingleTestResult =>
          new BarChart(CategoryAxis("Player"), NumberAxis("Number of matches")) {
            prefHeight = 300
            title = s"Number of matches per player (${singleResult.strategy.name})"
            legendVisible = false
            categoryGap = 0
            val seriesBuffer = ObservableBuffer[XYChart.Data[String, Number]]()
            seriesBuffer ++= singleResult.expectedResult.map {
              player => Data[String, Number](player.name, singleResult.games(player))
            }
            data() = ObservableBuffer(Series(seriesBuffer))
          }
        case _ => new Pane()
      }
    }
  }
}
