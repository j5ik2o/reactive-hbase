val scalaVersion211 = "2.11.12"
val scalaVersion212 = "2.12.8"

val awsSdk1Version = "1.11.492"
val awsSdk2Version = "2.4.0"

val catsVersion = "1.5.0"
val monixVersion = "3.0.0-RC2"
val akkaVersion = "2.5.19"

val compileScalaStyle = taskKey[Unit]("compileScalaStyle")

lazy val scalaStyleSettings = Seq(
  (scalastyleConfig in Compile) := file("scalastyle-config.xml"),
  compileScalaStyle := scalastyle.in(Compile).toTask("").value,
  (compile in Compile) := (compile in Compile)
    .dependsOn(compileScalaStyle)
    .value
)

val coreSettings = Seq(
  sonatypeProfileName := "com.github.j5ik2o",
  organization := "com.github.j5ik2o",
  scalaVersion := scalaVersion212,
  crossScalaVersions ++= Seq(scalaVersion211, scalaVersion212),
  scalacOptions ++= {
    Seq(
      "-feature",
      "-deprecation",
      "-unchecked",
      "-encoding",
      "UTF-8",
      "-language:_",
      "-Ydelambdafy:method",
      "-target:jvm-1.8"
    )
  },
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ =>
    false
  },
  pomExtra := {
    <url>https://github.com/j5ik2o/reactive-dynamodb</url>
      <licenses>
        <license>
          <name>The MIT License</name>
          <url>http://opensource.org/licenses/MIT</url>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:j5ik2o/reactive-dynamodb.git</url>
        <connection>scm:git:github.com/j5ik2o/reactive-dynamodb</connection>
        <developerConnection>scm:git:git@github.com:j5ik2o/reactive-dynamodb.git</developerConnection>
      </scm>
      <developers>
        <developer>
          <id>j5ik2o</id>
          <name>Junichi Kato</name>
        </developer>
      </developers>
  },
  publishTo in ThisBuild := sonatypePublishTo.value,
  credentials := {
    val ivyCredentials = (baseDirectory in LocalRootProject).value / ".credentials"
    Credentials(ivyCredentials) :: Nil
  },
  scalafmtOnCompile in ThisBuild := true,
  scalafmtTestOnCompile in ThisBuild := true,
  resolvers += Resolver.bintrayRepo("danslapman", "maven"),
  resolvers += Resolver.sonatypeRepo("releases"),
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.7"),
  libraryDependencies ++= Seq(
    "com.beachape" %% "enumeratum" % "1.5.13",
    "org.slf4j" % "slf4j-api" % "1.7.25",
    "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.0",
    "org.scalatest" %% "scalatest" % "3.0.5" % Test,
    "org.scalacheck" %% "scalacheck" % "1.14.0" % Test,
    "ch.qos.logback" % "logback-classic" % "1.2.3" % Test
  ),
  parallelExecution in Test := false,
  wartremoverErrors ++= Warts.allBut(Wart.Any,
                                     Wart.Throw,
                                     Wart.Nothing,
                                     Wart.Product,
                                     Wart.NonUnitStatements,
                                     Wart.DefaultArguments,
                                     Wart.ImplicitParameter,
                                     Wart.StringPlusAny,
                                     Wart.Overloading),
  wartremoverExcluded += baseDirectory.value / "src" / "test" / "scala"
) ++ scalaStyleSettings

lazy val test = (project in file("test"))
  .settings(
    coreSettings ++ Seq(
      name := "reactive-hbase-test",
      libraryDependencies ++= Seq(
        "com.google.guava" % "guava" % "25.1-jre",
        "commons-io" % "commons-io" % "2.6",
        "org.scalatest" %% "scalatest" % "3.0.5" % Provided,
        "com.whisk" %% "docker-testkit-scalatest" % "0.9.8",
        "com.whisk" %% "docker-testkit-impl-spotify" % "0.9.8",
        "org.apache.hbase" % "hbase-testing-util" % "2.1.3"
      )
    )
  )

lazy val core = (project in file("core")).settings(
  coreSettings ++ Seq(
    name := "reactive-hbase-core",
    libraryDependencies ++= Seq(
      "org.reactivestreams" % "reactive-streams" % "1.0.0",
      "org.apache.hadoop" % "hadoop-client" % "3.2.0",
      "org.apache.hbase" % "hbase-common" % "2.1.3",
      "org.apache.hbase" % "hbase-client" % "2.1.3"
    )
  )
)

lazy val cats = (project in file("cats")).settings(
  coreSettings ++ Seq(
    name := "reactive-hbase-cats",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % catsVersion,
      "org.typelevel" %% "cats-free" % catsVersion
    )
  )
) dependsOn (core, test % "test")

lazy val monix = (project in file("monix")).settings(
  coreSettings ++ Seq(
    name := "reactive-hbase-monix",
    libraryDependencies ++= Seq(
      "io.monix" %% "monix" % monixVersion
    )
  )
) dependsOn (core, test % "test")

lazy val akka = (project in file("akka")).settings(
  coreSettings ++ Seq(
    name := "reactive-hbase-akka",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
    )
  )
) dependsOn (core, test % "test")

lazy val `root` = (project in file("."))
  .settings(coreSettings)
  .settings(
    name := "reactive-hbase-project"
  )
  .aggregate(core, test, monix, cats, akka)
