val username = sys.env.get("SONATYPE_USERNAME")
  .orElse(Option(System.getProperty("SONATYPE_USERNAME")))

val password = sys.env
  .get("SONATYPE_PASSWORD")
  .orElse(Option(System.getProperty("SONATYPE_PASSWORD")))

credentials ++= (for {
  u <- username
  p <- password
} yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", u, p)).toSeq


sonatypeProfileName := "io.github.0lejk4"

publishMavenStyle := true
pgpPassphrase := password.map(_.toCharArray)
licenses := Seq(
  "APL2" -> url("https://github.com/0lejk4/Scalest/blob/master/LICENSE")
)

homepage := Some(url("https://github.com/0lejk4/Scalest"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/0lejk4/Scalest"),
    "scm:git@github.com:0lejk4/Scalest.git"
  )
)

useGpg := true
pgpReadOnly := false