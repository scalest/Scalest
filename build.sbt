val circeVersion = "0.10.0"
val akkaVersion = "2.5.20"
val slickVersion = "3.3.0"
val akkaHttpVersion = "10.1.7"
val scala2_12Version = "2.12.8"

lazy val snapshot: Boolean = true
lazy val v: String = {
  val vv = "0.0.3"
  if (!snapshot) vv
  else vv + "-SNAPSHOT"
}

organization in ThisBuild := "io.github.0lejk4"

def sonatypeProject(id: String, base: File) =
  Project(id, base)
    .settings(
      name := id,
      isSnapshot := snapshot,
      version := v,
      scalaVersion := scala2_12Version,
      publishTo := {
        val nexus = "https://oss.sonatype.org/"
        if (isSnapshot.value)
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else
          Some("releases" at nexus + "service/local/staging/deploy/maven2")
      },
      updateOptions := updateOptions.value.withGigahorse(false),
      scalacOptions ++= Seq("-Ypartial-unification", "-feature"),
      resolvers += Resolver.sonatypeRepo("releases"),
      pomExtra :=
        <developers>
          <developer>
            <id>0lejk4</id>
            <name>Oleh Dubynskiy</name>
            <url>https://github.com/0lejk4/</url>
          </developer>
        </developers>
      )

lazy val admin = sonatypeProject("scalest-admin", file("./admin"))
  .settings {
    version := v
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % "0.11.1",
      "io.circe" %% "circe-parser" % circeVersion,
      "com.propensive" %% "magnolia" % "0.10.0"
      )
  }

lazy val adminAkka = sonatypeProject("scalest-akka", file("./admin-akka"))
  .dependsOn(admin)
  .settings {
    version := v
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "de.heikoseeberger" %% "akka-http-circe" % "1.25.2",
      )
  }

lazy val adminSlick = sonatypeProject("scalest-admin-slick", file("./admin-slick"))
  .dependsOn(admin)
  .settings {
    version := v
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick" % slickVersion,
      "com.typesafe.slick" %% "slick-hikaricp" % slickVersion
      )
  }

lazy val examples = Project("scalest-examples", file("./examples"))
  .dependsOn(adminAkka, adminSlick)
  .settings {
    skip in publish := true
    publish := {}
    publishLocal := {}
    libraryDependencies ++= Seq(
      "com.h2database" % "h2" % "1.4.197",
      "io.circe" %% "circe-parser" % circeVersion
      )
  }

lazy val root = Project(id = "scalest", base = file("."))
  .aggregate(admin, adminAkka, adminSlick, examples)
  .settings {
    name := "scalest"
    version := v
    scalaVersion := scala2_12Version
    scalacOptions += "-Ypartial-unification"
    cancelable := true
    isSnapshot := snapshot
    skip in publish := true
    publish := {}
    publishLocal := {}
  }
