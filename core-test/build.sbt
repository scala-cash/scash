
name := "scash-test"

libraryDependencies ++= Deps.coreTest

publishArtifact := false

testOptions in Test += Tests.Argument(TestFrameworks.ScalaCheck, "-verbosity", "2")

coverageExcludedPackages := ".*gen"

coverageMinimum := 80

coverageFailOnMinimum := true
