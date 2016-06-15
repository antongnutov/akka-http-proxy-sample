package sample

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * @author Anton Gnutov
  */
class ApiManagerActor(host: String, port:Int, val originUrl: String) extends Actor with ActorLogging with RequestHandler {
  var binding: Option[Http.ServerBinding] = None
  implicit val system = context.system

  val http = Http(context.system)
  implicit val materializer = ActorMaterializer()
  implicit val timeout: Timeout = 15.seconds

  implicit val dispatcher = context.dispatcher

  val origin: HttpRequest => Future[HttpResponse] = { request =>
    val req: HttpRequest = request.copy(
      uri = originUrl + request.uri.path,
      headers = request.headers.filterNot(h => h.name() == "Timeout-Access")
    )
    http.singleRequest(req)
  }

  override def preStart(): Unit = {
    val selfRef = self
    Http().bindAndHandleAsync(requestHandler, host, port).foreach(bound => selfRef ! bound)
  }

  override def postStop(): Unit = {
    binding foreach (_.unbind())
  }

  override def receive: Receive = {
    case boundEvent: Http.ServerBinding =>
      log.info(s"API Started at: ${boundEvent.localAddress.toString}")
      binding = Some(boundEvent)
  }
}

object ApiManagerActor {
  def props(host: String, port: Int, originUrl: String): Props = Props(classOf[ApiManagerActor], host, port, originUrl)
}
