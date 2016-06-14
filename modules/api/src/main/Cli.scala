package oyun.api

import oyun.hub.actorApi.Deploy

private[api] final class Cli(bus: oyun.common.Bus) extends oyun.common.Cli {

  private val logger = oyun.log("cli")

  def apply(args: List[String]): Fu[String] = run(args).map(_ + "\n") ~ {
    _.logFailure(logger, _ => args mkString " ") foreach { output =>
      logger.info("%s\n%s".format(args mkString " ", output))
    }
  }

  def process = {
    case "deploy" :: "pre" :: Nil => remindDeploy(oyun.hub.actorApi.DeployPre)
    case "deploy" :: "post" :: Nil => remindDeploy(oyun.hub.actorApi.DeployPost)
  }

  private def remindDeploy(event: Deploy): Fu[String] = {
    bus.publish(event, 'deploy)
    fuccess("Deploy in progress")
  }

  private def run(args: List[String]): Fu[String] = {
    (processors lift args) | fufail("Unknown command: "+ args.mkString(" "))
  } recover {
    case e: Exception => "ERROR " + e
  }

  private def processors =
    oyun.i18n.Env.current.cli.process orElse
      process
  
}
