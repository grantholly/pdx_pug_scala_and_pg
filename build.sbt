lazy val root = (project in file("."))
  .settings(
    name := "pg",
    scalaVersion := "2.12.8",
    version := "0.1.0-SNAPSHOT",
    organization := "com.example",
    organizationName := "pg",
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick" % "3.3.0",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.3.0",
      "com.typesafe" % "config" % "1.3.2",
      "org.slf4j" % "slf4j-nop" % "1.6.4",
      "org.postgresql" % "postgresql" % "42.1.4",
    )
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
