val circeVersion = "0.10.0"
val akkaVersion = "2.5.20"
val izumiVersion = "0.6.29"
val slickVersion = "3.3.0"

lazy val core = Project("scalest-core", file("./core"))
  .settings {
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-http" % "10.1.7",
      "ch.megard" %% "akka-http-cors" % "0.3.4",
      "de.heikoseeberger" %% "akka-http-circe" % "1.24.3",
      "com.typesafe.slick" %% "slick" % slickVersion,
      "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
      "org.slf4j" % "slf4j-nop" % "1.6.4",
      "com.github.pshirshov.izumi.r2" %% "distage-core" % izumiVersion,
      "com.github.pshirshov.izumi.r2" %% "distage-config" % izumiVersion,
      "com.github.pshirshov.izumi.r2" %% "distage-cats" % izumiVersion,
      "com.github.pshirshov.izumi.r2" %% "distage-static" % izumiVersion,
      "com.github.pshirshov.izumi.r2" %% "distage-plugins" % izumiVersion,
      "com.github.pshirshov.izumi.r2" %% "distage-app" % izumiVersion
    )
  }

lazy val admin = Project("scalest-admin", file("./admin"))
  .dependsOn(core)
  .settings {
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "scalatags" % "0.6.7",
      "com.chuusai" %% "shapeless" % "2.3.3"
    )
  }

lazy val examples = Project("scalest-examples", file("./examples"))
  .dependsOn(
    admin,
    core
  )
  .settings {
    libraryDependencies ++= Seq(
      "com.h2database" % "h2" % "1.4.197"
    )
  }

lazy val root = Project(id = "scalest", base = file("."))
  .aggregate(
    core,
    admin,
    examples
  )
  .settings {
    name := "scalest"
    version := "0.1"
    scalaVersion := "2.12.8"
    scalacOptions += "-Ypartial-unification"
    cancelable := true
  }
