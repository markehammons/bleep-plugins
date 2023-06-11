import bleep.model.CrossProjectName

def projectsToPublish(crossName: CrossProjectName): Boolean =
  crossName.name.value match
    case "scoverage" => true
    case _           => false
