ThisBuild / scalaVersion := "2.13.8"

lazy val kvStore = (project in file("."))

lazy val stmBench = (project in file("stm-bench"))
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.3.11",
      "io.github.timwspence" %% "cats-stm" % "0.11.0"
    )
  )
  .enablePlugins(JmhPlugin)

lazy val yoloBench = (project in file("yolo-bench"))
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.3.11"
    )
  )
  .enablePlugins(JmhPlugin)
