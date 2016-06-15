package sample

import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model._

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Anton Gnutov
  */
trait RequestHandler {
  implicit val dispatcher: ExecutionContext

  val origin: HttpRequest => Future[HttpResponse]

  val requestHandler: HttpRequest => Future[HttpResponse] = {
    case req: HttpRequest if req.uri.path.startsWith(Path("/rest")) => origin(req)
    case _ => Future {
      HttpResponse(StatusCodes.NotFound)
    }
  }
}
