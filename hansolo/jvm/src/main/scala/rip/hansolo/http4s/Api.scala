package rip.hansolo.http4s

import org.http4s.headers.Host
import org.http4s.server._
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.staticcontent.ResourceService
import org.http4s.{HttpService, StaticFile, Request, Response}
import rip.hansolo.http4s.service._

import scalaz._

object Api extends App {

  import org.http4s.dsl._

  /**
    * Checks if the Host String fulfills the predicate
    *
    * @param predicate The function the domain is tested for
    * @return The transformed service returning NotFound if the host does not satisfy the predicate
    */
  def domainFilter(predicate: String => Boolean): HttpMiddleware = Middleware[Request, Response, Request, Response] {
    (req, service) => {
      val uriHost: Option[String] = req.uri.host.map(_.value)
      val headerHost: Option[String] = req.headers.get(Host).map(_.value)
      val hostString: Option[String] = uriHost.orElse(headerHost)

      import scalaz.syntax.std.option._

      val hostDisjunction = for {
        hostFound <- hostString \/> "no domain found in request"
        hostValid <- (Option(hostFound) filter predicate) \/> "host is not satisfying predicate"
      } yield hostValid

      hostDisjunction match {
        case -\/(message) =>
          println(message)
          NotFound()
        case \/-(host) =>
          println("domain is passing")
          service.run(req)
      }
    }
  }

  /**
    * A filter for domains used for testing
    */
  val testingDomainFilter = domainFilter((domain: String)  => domain.contains("test") || domain.contains("localhost"))

  import staticcontent.resourceService
  import ResourceService.Config

  // the 0.0.0.0 enables it to be picked up from outside
  BlazeBuilder.bindHttp(80, "0.0.0.0")
    .mountService(MainPageService(), "/")
    .mountService(testingDomainFilter(TellMeService()), "/info")
    .mountService(HelloWorldService(), "/hello")
    .mountService(GithubWebhookService(), "/webhook")
    .mountService(GameService(), "/game")
    .mountService(resourceService(Config(basePath = "")), "/public")
    .run
    .awaitShutdown()

}