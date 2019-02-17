val circeVersion = "0.10.0"
val akkaVersion = "2.5.20"
val izumiVersion = "0.6.29"
val slickVersion = "3.3.0"
val akkaHttpVersion = "10.1.7"

lazy val core = Project("scalest-core", file("./core"))
  .settings {
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % circeVersion,
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "de.heikoseeberger" %% "akka-http-circe" % "1.25.2",
      "com.github.pshirshov.izumi.r2" %% "distage-core" % izumiVersion,
      "com.github.pshirshov.izumi.r2" %% "distage-config" % izumiVersion,
      "ch.megard" %% "akka-http-cors" % "0.3.4"
    )
  }

lazy val admin = Project("scalest-admin", file("./admin"))
  .settings {
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "de.heikoseeberger" %% "akka-http-circe" % "1.25.2",
      "com.lihaoyi" %% "scalatags" % "0.6.7",
      "com.propensive" %% "magnolia" % "0.10.0"
    )
  }

lazy val adminSlick = Project("scalest-admin-slick", file("./admin-slick"))
  .dependsOn(admin)
  .settings {
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick" % slickVersion,
      "com.typesafe.slick" %% "slick-hikaricp" % slickVersion
    )
  }

lazy val examples = Project("scalest-examples", file("./examples"))
  .dependsOn(
    adminSlick,
    core
  )
  .settings {
    libraryDependencies ++= Seq(
      "com.h2database" % "h2" % "1.4.197",
      "io.circe" %% "circe-parser" % circeVersion
    )
  }

lazy val root = Project(id = "scalest", base = file("."))
  .aggregate(
    core,
    admin,
    adminSlick,
    examples
  )
  .settings {
    name := "scalest"
    version := "0.1"
    scalaVersion := "2.12.8"
    scalacOptions += "-Ypartial-unification"
    cancelable := true
  }
