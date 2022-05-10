ThisBuild / scalaVersion := "2.13.8"

lazy val kvStore = (project in file("kvstore"))
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.3.11",
      "io.github.timwspence" %% "cats-stm" % "0.11.0",
      "org.scalacheck" %% "scalacheck" % "1.14.1"
    )
  )
