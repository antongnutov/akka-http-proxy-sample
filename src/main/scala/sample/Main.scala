package sample

import akka.actor.ActorSystem

object Main extends App with AppConfig {
  val system = ActorSystem("sample")
  system.actorOf(ApiManagerActor.props(httpHost, httpPort, originUrl))
}