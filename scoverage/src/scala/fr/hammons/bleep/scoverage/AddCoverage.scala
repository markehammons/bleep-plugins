package fr.hammons.bleep.scoverage

import bleep.rewrites.BuildRewrite
import bleep.model.BuildRewriteName
import bleep.model.Build
import bleep.model.CrossProjectName
import bleep.model.Project
import bleep.model.Options

object AddCoverage extends BuildRewrite:
  override val name: BuildRewriteName = BuildRewriteName("add-coverage")

  override protected def newExplodedProjects(
      oldBuild: Build
  ): Map[CrossProjectName, Project] =
    oldBuild.explodedProjects.map: (crossName, project) =>
      if !project.isTestProject.getOrElse(false) then
        crossName -> project.copy(scala =
          project.scala.map(scala =>
            scala.copy(options =
              scala.options.union(
                Options.parse(
                  List(
                    "-coverage-out:${TARGET_DIR}/scoverage-report"
                  ),
                  None
                )
              )
            )
          )
        )
      else crossName -> project
