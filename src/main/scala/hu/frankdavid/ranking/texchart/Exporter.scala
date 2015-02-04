package hu.frankdavid.ranking.texchart


import java.awt.Color
import java.io.PrintWriter

import com.xeiam.xchart.StyleManager.{ChartTheme, ChartType, LegendPosition}
import com.xeiam.xchart.VectorGraphicsEncoder.VectorGraphicsFormat
import com.xeiam.xchart.{ChartBuilder, VectorGraphicsEncoder}
import hu.frankdavid.ranking.TournamentStrategy
import hu.frankdavid.ranking.gui.DefaultStrategies
import hu.frankdavid.ranking.strategy._
import hu.frankdavid.ranking.workbench.TestRunner

import scala.collection.JavaConversions._

object Exporter extends App {

  val ExportPath = args.lift(0).getOrElse {
    throw new RuntimeException("You must specify the export path as the first program argument.")
  }


  def exportChart[T: Numeric, S: Numeric](name: String, title: String = "", range: Iterable[T], xAxisTitle: String = "",
                                          yAxisTitle: String = "")
                                         (xLabel: (T => String))
                                         (data: (String, (T => S))*): Unit = {
    val chart = new ChartBuilder().chartType(ChartType.Bar).theme(ChartTheme.GGPlot2)
                .width(1000).height(600).title(title)
                .xAxisTitle(xAxisTitle).yAxisTitle(yAxisTitle)
                .build()
    chart.getStyleManager.setLegendPosition(LegendPosition.OutsideE)
    chart.getStyleManager.setChartBackgroundColor(Color.white)


    data.foreach { d =>
      chart.addSeries(d._1, range.map(xLabel),
        range.par.map(n => new java.lang.Double(implicitly[Numeric[S]].toDouble(d._2(n)))).seq)
    }

    VectorGraphicsEncoder.saveVectorGraphic(chart, ExportPath + "/" + name, VectorGraphicsFormat.PDF)
  }

  def exportCsv[R, C](name: String, rowRange: Iterable[R], columnRange: Iterable[C])(data: (R => C => _)): Unit = {
    val calculated = rowRange.map(r => columnRange.map(data(r)))
    exportCsv(name, calculated)
  }

  def exportCsv[R](name: String, rowRange: Iterable[R])(data: (R => _)): Unit = {
    val calculated = rowRange.par.map(row => (row, data(row)))
    val asIterableIterable = calculated.map {
      case (row, iterable: Iterable[_]) => row +: iterable.toSeq
      case (row, other) => Seq(row, other)
    }
    exportCsv(name, asIterableIterable.seq)
  }

  def exportCsv(name: String, data: Iterable[Iterable[_]]): Unit = {
    val writer = new PrintWriter(ExportPath + "/" + name + ".csv")
    for (row <- data) {
      writer.println(row.mkString("\t"))
    }
    writer.close()
  }

  def doubleLabel(x: Double) = x.formatted("%.2f")

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


  def strategyAbbreviation(strategy: TournamentStrategy): String = strategy.name.split(' ').head.toLowerCase

  {
    val strategy = MergeStrategy
    val abbr = strategyAbbreviation(strategy)
    exportCsv(s"${abbr}_perfect_player_matches_histogram", {
      val test = TestRunner(32, 3, 32, 0, 0).run(strategy)
      test.expectedResult.zipWithIndex.map { case (player, i) =>
        Seq(i + 1, test.games(player))
      }
    })
  }

  {
    exportCsv("football_cup_accuracy", 0.0 to 1.0 by 0.2) { resultPrediction: Double =>
      val test = TestRunner(32, 3, 32, 0, resultPrediction).runMany(FootballWorldCupStrategy, 1000)
      List(test.placeGuessedCorrectly(0), test.placeGuessedCorrectly(1))
    }
  }


  {
    val results = TestRunner(32, 4, 32, 0, 0).runMany(FootballWorldCupStrategy, 1000)
    exportCsv("follow1234", 0 to 3, 0 to 3)(results.followPlace)

    val results2 = TestRunner(32, 4, 32, 1, 0).runMany(FootballWorldCupStrategy, 1000)
    exportCsv("follow1234_2", 0 to 3, 0 to 3)(results2.followPlace)
  }


  {
    exportCsv("merge_number_of_rounds", 0.0 to 1.0 by 0.2) { resultPrediction: Double =>
      val test = TestRunner(32, 4, 32, 0, 0).runMany(FootballWorldCupStrategy, 1000)
      List(test.numberOfRounds, test.numberOfGames)
    }
  }

  {
    exportCsv("merge_no_matches_rounds", 2 to 100 by 2) { players: Int =>
      val test = TestRunner(players, 3, players, 0, 0).run(MergeStrategy)
      Seq(test.numberOfGames, test.numberOfRounds)
    }
  }

  {
    exportCsv("merge_no_matches_rounds_topk", 1 to 128) { topk: Int =>
      val test = TestRunner(128, topk, 128, 0, 0).run(MergeStrategy)
      Seq(test.numberOfGames, test.numberOfRounds)
    }
  }

  exportCsv("swiss_result_difference", 0.0 to 1.0 by 0.25) { inaccuracy: Double =>
    val test1 = TestRunner(16, 16, 16, 0, inaccuracy).runMany(new TraditionalSwissStrategy(_ => 7), 1000)
    val test2 = TestRunner(16, 16, 16, 0, inaccuracy).runMany(new ColleyStrategy(_ => 7), 1000)
    val test3 = TestRunner(16, 16, 16, 0, inaccuracy).runMany(new MarkovChainStrategy(_ => 7), 1000)
    Seq(test1.resultDistance, test2.resultDistance, test3.resultDistance)
  }

  exportCsv("swiss_diff_rounds", 2 to 32 by 2) { rounds: Int =>
    println(rounds)
    val test1 = TestRunner(32, 32, 32, 0.3, 1).runMany(new TraditionalSwissStrategy(_ => rounds), 200)
    val test2 = TestRunner(32, 32, 32, 0.3, 1).runMany(new ColleyStrategy(_ => rounds), 200)
    val test3 = TestRunner(32, 32, 32, 0.3, 1).runMany(new MarkovChainStrategy(_ => rounds), 200)
    Seq(test1.resultDistance, test2.resultDistance, test3.resultDistance)
  }


  exportCsv("swiss_result_difference_skill_var", 0.0 to 1.0 by 0.25) { skill: Double =>
    val test1 = TestRunner(16, 16, 16, skill, 1).runMany(new TraditionalSwissStrategy(_ => 7), 1000)
    val test2 = TestRunner(16, 16, 16, skill, 1).runMany(new ColleyStrategy(_ => 7), 1000)
    val test3 = TestRunner(16, 16, 16, skill, 1).runMany(new MarkovChainStrategy(_ => 7), 1000)
    Seq(test1.resultDistance, test2.resultDistance, test3.resultDistance)
  }

  exportCsv("result_graph_matches", 0.0 to 1.0 by 0.25) { inaccuracy: Double =>
    val strategies = Seq(GreedyGraphStrategy, MergeStrategy, OneByOneStrategy(GreedyGraphStrategy),
      OneByOneStrategy(MergeStrategy))
    strategies.map(strat =>
      TestRunner(32, 3, 32, 0, inaccuracy).runMany(strat, 5000).numberOfGames
    )
  }

  exportCsv("result_graph_rounds", 0.0 to 1.0 by 0.25) { inaccuracy: Double =>
    val strategies = Seq(GreedyGraphStrategy, MergeStrategy, OneByOneStrategy(GreedyGraphStrategy),
      OneByOneStrategy(MergeStrategy))
    strategies.map(strat =>
      TestRunner(32, 3, 32, 0, inaccuracy).runMany(strat, 5000).numberOfRounds
    )
  }

  exportCsv("result_graph_matches_rounds_players", 5 to 100 by 5) { players: Int =>
    val strategies = Seq(GreedyGraphStrategy, MergeStrategy, OneByOneStrategy(GreedyGraphStrategy),
      OneByOneStrategy(MergeStrategy))
    strategies.flatMap(strat => {
      val test = TestRunner(players, 3, players, 0, 0).runMany(strat, 1000)
      Seq(
        test.numberOfGames,
        test.numberOfRounds
      )
    })
  }

  exportCsv("result_graph_matches_rounds_topk", 1 to 32 by 1) { k: Int =>
    val strategies = Seq(GreedyGraphStrategy, MergeStrategy, OneByOneStrategy(GreedyGraphStrategy),
      OneByOneStrategy(MergeStrategy))
    strategies.flatMap(strat => {
      val test = TestRunner(32, k, 32, 0, 0).runMany(strat, 1000)
      Seq(
        test.numberOfGames,
        test.numberOfRounds
      )
    })
  }

  exportCsv("jre_required_matches", 0.0 to 1.0 by 0.2) { inaccuracy: Double =>
    val strategies = Seq(TimSortStrategy(_ => 32), TimSortStrategy(), HeapStrategy(_ => 32), HeapStrategy())
    strategies.map(strat => {
      val test = TestRunner(32, 32, 32, 0, inaccuracy).runMany(strat, 200)
      test.numberOfGames
    }
    )
  }

  exportCsv("jre_required_rounds", 0.0 to 1.0 by 0.2) { inaccuracy: Double =>
    val strategies = Seq(TimSortStrategy(_ => 32), TimSortStrategy(), HeapStrategy(_ => 32), HeapStrategy())
    strategies.map(strat => {
      val test = TestRunner(32, 32, 32, 0, inaccuracy).runMany(strat, 200)
      test.numberOfRounds
    }
    )
  }

  exportCsv("jre_required_matches_", 0.0 to 1.0 by 0.2) { inaccuracy: Double =>
    val strategies = Seq(TimSortStrategy(_ => 32), TimSortStrategy(), HeapStrategy(_ => 32), HeapStrategy())
    strategies.map(strat => {
      val test = TestRunner(32, 32, 32, 0, inaccuracy).runMany(strat, 200)
      test.numberOfGames
    }
    )
  }

  exportCsv("jre_required_rounds", 0.0 to 1.0 by 0.2) { inaccuracy: Double =>
    val strategies = Seq(TimSortStrategy(_ => 32), TimSortStrategy(), HeapStrategy(_ => 32), HeapStrategy())
    strategies.map(strat => {
      val test = TestRunner(32, 32, 32, 0, inaccuracy).runMany(strat, 200)
      test.numberOfRounds
    }
    )
  }

  {
    val res = (16 to 80 by 16).par.map { players =>
      (players, DefaultStrategies.map { strat =>
        val test = TestRunner(players, 3, players, 0.1, 0.3).runMany(strat, 100)
        println(s"$players $strat")
        (test.numberOfGames, test.numberOfRounds, test.resultDistance)
      })
    }.seq
    exportCsv("all_matches", res.map { x => x._1 +: x._2.map(_._1)})
    exportCsv("all_rounds", res.map { x => x._1 +: x._2.map(_._2)})
    exportCsv("all_distance", res.map { x => x._1 +: x._2.map(_._3)})
  }

  exportCsv("all_diff", 0.0 to 1.01 by 0.2) { skill: Double =>
    DefaultStrategies.map { strat =>
      println(s"$skill, $strat")
      TestRunner(32, 3, 32, skill, 1).runMany(strat, 200).resultDistance
    }
  }


}
