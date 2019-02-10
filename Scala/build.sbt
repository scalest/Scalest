import scala.sys.process._

val V = new {
  val circe = "0.13.0"
  val catsEffect = "2.1.2"
  val slick = "3.3.2"
  val akkaHttp = "10.1.11"
  val akkaHttpCirce = "1.31.0"
  val scala = "2.13.1"
  val http4s = "0.21.1"
  val magnolia = "0.12.8"
  val tapir = "0.12.23"
  val chimney = "0.4.2"
  val jwt = "4.2.0"
  val scalaLogging = "3.9.2"
  val kindProjector = "0.11.0"
}

inThisBuild(
  List(
    organization := "io.github.0lejk4",
    homepage := Some(url("https://github.com/scalest/Scalest")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
          Developer(
            "0lejk4",
            "Oleh Dubynskiy",
            "",
            url("https://github.com/0lejk4"),
          ),
        ),
    scalaVersion := V.scala,
    scalacOptions ++= Seq(
          "-Ymacro-annotations",
        ),
  ),
)

name := "scalest"
skip in publish := true
cancelable in ThisBuild := true

lazy val docs = project
  .in(file("./scalest-docs"))
  .settings(
    mdocVariables := Map(
          "VERSION" -> version.value,
        ),
    skip in publish := true,
  )
  .enablePlugins(MdocPlugin, DocusaurusPlugin)

lazy val circeSettings = Seq(
  libraryDependencies ++= Seq(
        "io.circe" %% "circe-core" % V.circe,
        "io.circe" %% "circe-parser" % V.circe,
        "io.circe" %% "circe-generic" % V.circe,
        "io.circe" %% "circe-generic-extras" % V.circe,
      ),
)

lazy val noPublishSettings = Seq(
  skip in publish := true,
)

lazy val buildAdminUi = TaskKey[Unit]("buildAdminUi", "/Build admin-ui")

lazy val adminUiSettings = Seq(
  unmanagedResourceDirectories in Compile += file("../admin-ui/build/backend/"),
  unmanagedResourceDirectories in Compile += file("../assets/"),
)

lazy val buildAdminUiSettings = Seq(
  buildAdminUi := {
    "npm run build --prefix ../admin-ui".!
  },
  compile in Compile := (compile in Compile).dependsOn(buildAdminUi).value,
)

lazy val scalestValidation = Project("scalest-validation", file("./validation"))
  .dependsOn(scalestCore)
  .settings(circeSettings: _*)

lazy val scalestCore = Project("scalest-core", file("./core"))
  .dependsOn(scalestMeta)
  .settings(circeSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
          "org.typelevel" %% "cats-effect" % V.catsEffect,
          "com.propensive" %% "magnolia" % V.magnolia,
          "io.scalaland" %% "chimney" % V.chimney,
        ),
  )

lazy val scalestTapir = Project("scalest-tapir", file("./scalest-tapir"))
  .dependsOn(scalestCore)
  .settings(buildAdminUiSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
          "com.softwaremill.sttp.tapir" %% "tapir-core" % V.tapir,
          "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % V.tapir,
          "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % V.tapir,
          "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % V.tapir,
        ),
  )

lazy val scalestMeta = Project("scalest-meta", file("./scalest-meta"))
  .settings(
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  )

lazy val kindProjectorSettings = Seq(
  addCompilerPlugin(("org.typelevel" % "kind-projector" % V.kindProjector).cross(CrossVersion.full)),
)

lazy val scalestAdmin = Project("admin", file("./admin"))
  .settings(kindProjectorSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
          "org.typelevel" %% "cats-effect" % V.catsEffect,
          "com.pauldijou" %% "jwt-core" % V.jwt,
          "com.pauldijou" %% "jwt-circe" % V.jwt,
          "com.typesafe.scala-logging" %% "scala-logging" % V.scalaLogging,
        ),
  )
  .dependsOn(scalestTapir, scalestHealthcheck)

lazy val scalestUsers = Project("scalest-users", file("./scalest-users"))
  .settings(kindProjectorSettings: _*)
  .dependsOn(scalestAdmin)

lazy val scalestPages = Project("scalest-pages", file("./scalest-pages"))
  .settings(kindProjectorSettings: _*)
  .dependsOn(scalestAdmin)

lazy val scalestVersions = Project("scalest-versions", file("./scalest-versions"))
  .settings(kindProjectorSettings: _*)
  .dependsOn(scalestAdmin)

lazy val scalestMigrate = Project("scalest-migrate", file("./scalest-migrate"))
  .settings(kindProjectorSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
          "org.typelevel" %% "cats-effect" % V.catsEffect,
        ),
  )

lazy val scalestAdminAkka = Project("admin-akka", file("./admin-akka"))
  .settings(adminUiSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
          "com.typesafe.akka" %% "akka-http" % V.akkaHttp,
          "de.heikoseeberger" %% "akka-http-circe" % V.akkaHttpCirce,
          "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server" % V.tapir,
        ),
  )
  .dependsOn(scalestCore, scalestValidation, scalestAdmin)

lazy val scalestAdminHttp4s = Project("admin-http4s", file("./admin-http4s"))
  .settings(kindProjectorSettings: _*)
  .settings(adminUiSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
          "org.http4s" %% "http4s-dsl" % V.http4s,
          "org.http4s" %% "http4s-blaze-server" % V.http4s,
          "org.http4s" %% "http4s-blaze-client" % V.http4s,
          "org.http4s" %% "http4s-circe" % V.http4s,
          "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % V.tapir,
        ),
  )
  .dependsOn(scalestCore, scalestValidation, scalestAdmin)

lazy val scalestHealthcheck = Project("scalest-healthcheck", file("./healthcheck"))
  .settings(circeSettings: _*)

lazy val scalestSlick = Project("scalest-slick", file("./slick"))
  .dependsOn(scalestTapir, scalestHealthcheck)
  .settings(
    libraryDependencies ++= Seq(
          "com.typesafe.slick" %% "slick" % V.slick,
          "com.typesafe.slick" %% "slick-hikaricp" % V.slick,
        ),
  )

lazy val examples = project
  .settings(scalaVersion := V.scala)
  .settings(noPublishSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
          "com.h2database" % "h2" % "1.4.197",
          "io.circe" %% "circe-parser" % V.circe,
          "ch.qos.logback" % "logback-classic" % "1.2.3",
          "dev.zio" %% "zio" % "1.0.0-RC18",
          "dev.zio" %% "zio-interop-cats" % "2.0.0.0-RC11",
        ),
    fork in run := true,
    cancelable in Global := true,
  )
  .dependsOn(scalestAdminAkka, scalestAdminHttp4s, scalestSlick, scalestVersions, scalestUsers, scalestPages)
