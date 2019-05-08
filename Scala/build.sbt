import scala.sys.process._

val circeVersion = "0.12.2"
val catsEffectVersion = "2.0.0"
val akkaVersion = "2.6.1"
val slickVersion = "3.3.2"
val akkaHttpVersion = "10.1.11"
val projectScalaVersion = "2.13.1"
val http4sVersion = "0.21.0-M6"
val jwtCoreVersion = "4.1.0"
val magnoliaVersion = "0.12.5"
val tapirVersion = "0.12.12"
val akkaHttpCirceVersion = "1.30.0"
val chimneyVersion = "0.3.5"

lazy val snapshot: Boolean = true
val versionNumber = "0.0.4"

lazy val projectVersion: String = {
  if (!snapshot) versionNumber
  else versionNumber + "-SNAPSHOT"
}

organization in ThisBuild := "io.github.0lejk4"

def scalestProject(id: String, base: File) =
  Project(id, base)
    .settings(
      name := id,
      isSnapshot := snapshot,
      version := projectVersion,
      scalaVersion := projectScalaVersion,
      publishTo := {
        val nexus = "https://oss.sonatype.org/"
        if (isSnapshot.value)
          Some("snapshots".at(nexus + "content/repositories/snapshots"))
        else
          Some("releases".at(nexus + "service/local/staging/deploy/maven2"))
      },
      updateOptions := updateOptions.value.withGigahorse(false),
      scalacOptions ++= Seq("-feature"),
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

lazy val circeSettings = Seq(
  libraryDependencies ++= Seq(
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-generic-extras" % circeVersion
  )
)

lazy val noPublishSettings = Seq(
  skip in publish := true,
  publish := {},
  publishLocal := {}
)

lazy val cancelableSettings = Seq(
  fork in run := true,
  cancelable in Global := true
)

lazy val buildAdminUi = TaskKey[Unit]("buildAdminUi", "/Build admin-ui")

lazy val adminUiSettings = Seq(
  unmanagedResourceDirectories in Compile += file("../admin-ui/build/backend/"),
  unmanagedResourceDirectories in Compile += file("../assets/")
)

lazy val buildAdminUiSettings = Seq(
  buildAdminUi := {
    "npm run build --prefix ../admin-ui".!
  },
  compile in Compile := (compile in Compile).dependsOn(buildAdminUi).value,
)

lazy val scalestValidation = scalestProject("scalest-validation", file("./validation"))
  .dependsOn(scalestCore)
  .settings(circeSettings: _*)

lazy val scalestCore = scalestProject("scalest-core", file("./core"))
  .dependsOn(scalestMeta)
  .settings(circeSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.propensive" %% "magnolia" % magnoliaVersion,
      "io.scalaland" %% "chimney" % chimneyVersion
    )
  )

lazy val scalestTapir = scalestProject("scalest-tapir", file("./scalest-tapir"))
  .dependsOn(scalestCore)
  .settings(buildAdminUiSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion
    )
  )

lazy val scalestMeta = scalestProject("scalest-meta", file("./scalest-meta"))
  .settings(
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
  )

lazy val scalestAdmin = scalestProject("admin-tapir", file("./admin-tapir"))
  .settings(
    libraryDependencies += "org.typelevel" %% "cats-effect" % catsEffectVersion
  )
  .dependsOn(scalestTapir, scalestHealthcheck)

lazy val scalestAdminAkka = scalestProject("admin-akka", file("./admin-akka"))
  .settings(adminUiSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "de.heikoseeberger" %% "akka-http-circe" % akkaHttpCirceVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server" % tapirVersion
    )
  )
  .dependsOn(scalestCore, scalestValidation, scalestAdmin)

lazy val scalestAdminHttp4s = scalestProject("admin-http4s", file("./admin-http4s"))
  .settings(adminUiSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion
    )
  )
  .dependsOn(scalestCore, scalestValidation, scalestAdmin)

lazy val scalestHealthcheck = scalestProject("scalest-healthcheck", file("./healthcheck"))
  .settings(circeSettings: _*)

lazy val scalestSlick = scalestProject("scalest-slick", file("./slick"))
  .dependsOn(scalestTapir, scalestHealthcheck)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick" % slickVersion,
      "com.typesafe.slick" %% "slick-hikaricp" % slickVersion
    )
  )

lazy val examples = Project("scalest-examples", file("./examples"))
  .settings(scalaVersion := projectScalaVersion)
  .settings(cancelableSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.h2database" % "h2" % "1.4.197",
      "io.circe" %% "circe-parser" % circeVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "dev.zio" %% "zio" % "1.0.0-RC17",
      "dev.zio" %% "zio-interop-cats" % "2.0.0.0-RC10"
    )
  )
  .dependsOn(scalestAdminAkka, scalestAdminHttp4s, scalestSlick)

lazy val scalest = Project(id = "root", base = file("."))
  .aggregate(scalestCore, scalestAdminAkka, scalestAdmin, scalestSlick, scalestAdminHttp4s, examples, scalestMeta, scalestTapir, scalestValidation, scalestHealthcheck)
  .settings(scalaVersion := projectScalaVersion)
  .settings(noPublishSettings: _*)
