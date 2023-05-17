organization := "com.arcadia"

scalaVersion := "2.12.17"
name := "datamanipulator"

version := "0.0.1"

scalacOptions ++= Seq("-deprecation", "-unchecked")

parallelExecution in Test := false

fork := true

coverageHighlighting := true

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql" % "3.3.2",
  "org.apache.spark" %% "spark-core" % "3.3.2",
  "org.scalatest" %% "scalatest" % "3.2.2" % "test",
  "org.scalacheck" %% "scalacheck" % "1.15.2" % "test",
  "com.holdenkarau" %% "spark-testing-base" % "3.3.1_1.3.0" % "test",
  "org.scalameta" %% "munit" % "0.7.26" % Test,
)

scalacOptions ++= Seq("-deprecation", "-unchecked")

pomIncludeRepository := { x => false }

resolvers ++= Seq(
  "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/",
  "Second Typesafe repo" at "https://repo.typesafe.com/typesafe/maven-releases/",
  Resolver.sonatypeRepo("public")
)

pomIncludeRepository := { _ => false }

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

unmanagedResourceDirectories in Compile += { baseDirectory.value / "src/resources/timeusage" }