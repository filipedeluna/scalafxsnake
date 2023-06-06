name := "snake"
version := "0.1"
scalaVersion := "3.2.2"
scalacOptions ++= Seq("-language:higherKinds")

libraryDependencies += "org.scalafx" %% "scalafx" % "20.0.0-R31"
libraryDependencies ++= Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
  .map(m => "org.openjfx" % s"javafx-$m" % "16" classifier "linux")

mainClass in Compile := Some("snake.Snake")