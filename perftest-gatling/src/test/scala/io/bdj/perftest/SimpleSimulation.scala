package io.bdj.perftest

import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._
import io.gatling.core.Predef._

import scala.concurrent.duration._

/**
  *
  */
class SimpleSimulation extends Simulation {

  val default = http
    .acceptHeader("application/json, text/javascript, */*; q=0.01")
    .acceptEncodingHeader("gzip, deflate, sdch, br")
    .acceptLanguageHeader("en-US,en;q=0.8,de-DE;q=0.6,de;q=0.4")
    .userAgentHeader("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36")


  val httpProtocol = default.baseURL("http://localhost:18080")
    .disableWarmUp
    .inferHtmlResources
    .maxConnectionsPerHostLikeChrome
    .disableResponseChunksDiscarding
    .disableFollowRedirect

  val acceptHtml = Map(
    "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
    "Accept-Encoding" -> "gzip, deflate, sdch, br",
    "Accept-Language" -> "en-US,en;q=0.8,de-DE;q=0.6,de;q=0.4",
    "Connection" -> "keep-alive",
    "Upgrade-Insecure-Requests" -> "1",
    "User-Agent" -> "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.71 Safari/537.36")


  val scn = scenario("Simple").exec(http("request_0")
    .get("/")
    .headers(acceptHtml))


  setUp(
    scn.inject(
      constantUsersPerSec(500) during (1 minute),
      constantUsersPerSec(750) during (1 minute),
      constantUsersPerSec(900) during (1 minute),
      constantUsersPerSec(1000) during (1 minute),
      constantUsersPerSec(1500) during (1 minute),
      constantUsersPerSec(2000) during (1 minute)

    )
  ).protocols(httpProtocol)

}
