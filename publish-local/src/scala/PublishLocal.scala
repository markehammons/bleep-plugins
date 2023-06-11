import bleep.BleepScript
import bleep.Commands
import bleep.Started
import bleep.plugin.dynver.DynVerPlugin
object PublishLocal extends BleepScript("PublishLocal"):
  def run(started: Started, commands: Commands, args: List[String]): Unit =
    val dynVer = new DynVerPlugin(
      baseDirectory = started.buildPaths.buildDir.toFile().nn,
      dynverSonatypeSnapshots = true,
      dynverTagPrefix = Some("v")
    )

    val projects =
      started.build.explodedProjects.keys.toArray.filter(projectsToPublish)

    commands.publishLocal(
      bleep.commands.PublishLocal.Options(
        groupId = "fr.hammons",
        version = dynVer.version,
        publishTarget = bleep.commands.PublishLocal.LocalIvy,
        projects = projects
      )
    )
