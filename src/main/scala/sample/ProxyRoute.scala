package sample

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.Future

object ProxyRoute extends App with AppConfig {

  implicit val system = ActorSystem("sample")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val http = Http(system)

  val origin: HttpRequest => Future[HttpResponse] = { request =>
    val req: HttpRequest = request.copy(
      uri = originUrl + request.uri.path,
      headers = request.headers.filterNot(h => h.name() == "Timeout-Access")
    )
    http.singleRequest(req)
  }

  val requestHandler: HttpRequest => Future[HttpResponse] = {
    case req: HttpRequest if req.uri.path.startsWith(Path("/rest")) => origin(req)
    case _ => Future {
      HttpResponse(StatusCodes.NotFound)
    }
  }

  val proxyRoute: Route = Route { context =>
    val flow = Flow[HttpRequest].mapAsync(1)(requestHandler)

    val handler = Source.single(context.request)
      .via(flow)
      .runWith(Sink.head)
      .flatMap(context.complete(_))
    handler
  }

  val binding = Http(system).bindAndHandle(handler = proxyRoute, interface = httpHost, port = httpPort)
}
