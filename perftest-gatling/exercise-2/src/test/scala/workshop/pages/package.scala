package workshop

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import workshop.pages.Headers._

/**
  * List of Pages of the WebShop
  */
package object pages {

  /**
    * Headers used in the requests
    */
  object Headers {
    val defaultHeader = Map(
      "Accept" -> "*/*",
      "Accept-Encoding" -> "gzip, deflate",
      "Accept-Language" -> "en-US,de;q=0.7,en;q=0.3",
      "Connection" -> "keep-alive",
      "Pragma" -> "no-cache",
      "User-Agent" -> "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0")

    val acceptCss = Map("Accept" -> "text/css,*/*;q=0.1")

    val acceptImage = Map("Accept" -> "image/webp,image/*,*/*;q=0.8")

    val formHeader = Map(
      "Accept-Encoding" -> "gzip, deflate, br")
  }

  /**
    * this page definition is a group of executions, with a single execution containing a sequence of http
    * request that comprise the page. The Grouping helps in the generated reports that all explicit and
    * implicit requests (i.e. inferred resource) are grouped into one group (with summary stats, like page load
    * for the group) which improves readability of the report.
    */
  def WelcomePage = group("Welcome Page") {
    exec(http("/")
      .get("/")
      .headers(defaultHeader)
      //resources are loaded in parallel
      .resources(
      http("/Welcome.action")
        .get("/Welcome.action")
        .headers(defaultHeader),
      http("/style/bdj.css")
        .get("/style/bdj.css")
        .headers(acceptCss)))
  }

  /*
   * this page definition is just the plain sequence of http requests. The sequence is the same as in the
   * WelcomePage above, but without grouping
   */
  def Welcome = http("/")
    .get("/")
    .headers(defaultHeader)
    //resources are loaded in parallel
    .resources(
    http("/Welcome.action")
      .get("/Welcome.action")
      .headers(defaultHeader),
    http("/style/bdj.css")
      .get("/style/bdj.css")
      .headers(acceptCss))


  def JuicesPage = group("Juices Page") {
    exec(http("/products_juices.action")
      .get("/products_juices.action")
      .headers(defaultHeader))
  }

  //exercise: add more pages here
}
