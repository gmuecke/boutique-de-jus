package workshop

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class AnonymousVisitSimulation extends Simulation {

  val httpProtocol = http
    .baseURL("http://localhost:8080")
    .inferHtmlResources()
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate, sdch, br")
    .acceptLanguageHeader("en-US,en;q=0.8,de-DE;q=0.6,de;q=0.4")
    .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36")

  val headers_0 = Map(
    "Accept" -> "*/*",
    "Accept-Encoding" -> "gzip, deflate",
    "Accept-Language" -> "en-US,de;q=0.7,en;q=0.3",
    "Connection" -> "keep-alive",
    "Pragma" -> "no-cache",
    "User-Agent" -> "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0")

  val headers_1 = Map("Upgrade-Insecure-Requests" -> "1")

  val headers_3 = Map("Accept" -> "text/css,*/*;q=0.1")

  val headers_8 = Map("Accept" -> "image/webp,image/*,*/*;q=0.8")

  val headers_17 = Map(
    "Accept" -> "*/*",
    "Accept-Encoding" -> "gzip, deflate, br",
    "Content-Type" -> "application/json",
    "Origin" -> "chrome-extension://ompiailgknfdndiefoaoiligalphfdae")

  val headers_18 = Map(
    "Accept-Encoding" -> "gzip, deflate, br",
    "Origin" -> "http://localhost:8080",
    "Upgrade-Insecure-Requests" -> "1")

  val welcomePage = http("Index Page")
    .get("/")
    .headers(headers_1)
    .resources(
      http("Welcome Page")
        .get("/Welcome.action")
        .headers(headers_1),
      http("Stylesheet")
        .get("/style/bdj.css")
        .headers(headers_3))

  val productPage = http("Juices Page")
    .get("/products_juices.action")
    .headers(headers_1)

  val scn = scenario("RecordedSimulation")
    .exec(welcomePage)
    .pause(10)
    .exec(productPage)
    .pause(10)
    .exec(http("Accessoires Page")
      .get("/products_accessoires.action")
      .headers(headers_1))
    .pause(10)
    .exec(http("Books Page")
      .get("/products_books.action")
      .headers(headers_1))
    .exec(http("Courses Page")
      .get("/products_courses.action")
      .headers(headers_1))
    .pause(4)

  setUp(
    scn.inject(
      atOnceUsers(1)
    )
  ).protocols(httpProtocol)
}
