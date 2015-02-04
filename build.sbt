name := "pingpong"

scalaVersion := "2.11.4"

scalacOptions += "-target:jvm-1.8"

sourceDirectory := new File("/Users/david/Dropbox/IdeaProjects/pingpong/src")

mainClass in Compile := Some("hu.frankdavid.ranking.gui.Main")

version := "1.0"

resolvers += "erichseifert.de" at "http://mvn.erichseifert.de/maven2"

libraryDependencies += "org.scalafx" %% "scalafx" % "8.0.20-R6"

libraryDependencies += "org.controlsfx" % "controlsfx" % "8.20.8"

libraryDependencies += "org.apache.commons" % "commons-math3" % "3.3"

libraryDependencies += "com.xeiam.xchart" % "xchart" % "2.4.2"

libraryDependencies += "org.scalanlp" % "breeze_2.10" % "0.9"

