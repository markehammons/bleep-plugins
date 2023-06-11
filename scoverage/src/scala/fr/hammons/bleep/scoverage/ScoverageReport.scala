package fr.hammons.bleep.scoverage

import bleep.BleepScript
import bleep.rewrites.BuildRewrite
import bleep.Commands
import bleep.Started
import java.nio.file.Files
import bleep.PathOps
import scala.jdk.CollectionConverters.*
import scala.jdk.StreamConverters.*
import java.nio.file.Paths
import scala.util.chaining.*
import scoverage.reporter.CoverageAggregator
import scoverage.reporter.ScoverageXmlWriter

object ScoverageReport extends BleepScript("ScoverageReport"):
  override val rewrites: List[BuildRewrite] = List(AddCoverage)

  def run(started: Started, commands: Commands, args: List[String]): Unit =
    val testProjects = started.build.explodedProjects
      .filter((_, p) => p.isTestProject.getOrElse(false))
      .keySet
      .toList

    val projectPaths = started.build.explodedProjects
      .filter((_, project) => !project.isTestProject.getOrElse(false))
      .map(started.buildPaths.project)

    projectPaths
      .foreach: projectPath =>
        Files.createDirectories(projectPath.targetDir / "scoverage-report")

    commands.compile(started.build.explodedProjects.keys.toList)

    commands.test(
      started.build.explodedProjects
        .filter((_, project) => !project.isTestProject.getOrElse(false))
        .keys
        .toList
    )

    projectPaths
      .flatMap: projectPath =>
        Files
          .list(projectPath.targetDir / "scoverage-report")
          .nn
          .toScala(List)
      .filter(_.endsWith("scoverage.coverage"))
      .foreach: coverageReport =>
        val modifiedReport = Files
          .readAllLines(coverageReport)
          .nn
          .asScala
          .map: string =>
            if string.startsWith("../") then
              started.buildPaths.buildDir
                .toAbsolutePath()
                .nn
                .relativize(Paths.get(string.stripPrefix("..")))
                .nn
                .toString()
            else string
          .mkString("\n")

        Files.write(coverageReport, modifiedReport.getBytes())

    val coverage = projectPaths
      .map: projectPath =>
        projectPath.sourcesDirs.all -> projectPath.targetDir / "scoverage-reports"
      .map: (sources, data) =>
        CoverageAggregator
          .aggregate(
            Seq(data.toFile().nn),
            started.buildPaths.buildDir.toFile().nn
          )
          .pipe((sources, data, _))

    coverage.foreach: (sources, dataDir, coverage) =>
      ScoverageXmlWriter(
        sources.map(_.toFile().nn).toSeq,
        dataDir.toFile().nn,
        false,
        None
      ).write(coverage.get)
