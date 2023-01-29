val scala3Version = "3.3.0-RC1"

lazy val root = project
  .in(file("."))
  .settings(
    name                                   := "loom-strand-pattern",
    version                                := "0.1.0-SNAPSHOT",
    scalaVersion                           := scala3Version,
    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test,
    javacOptions ++= Seq("-source", "19", "-target", "19"),
    watchTriggeredMessage := Watch.clearScreenOnTrigger,
    watchBeforeCommand    := Watch.clearScreen
  )
