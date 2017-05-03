package workshop

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder
import workshop.Headers.{acceptCss, acceptImage, defaultHeader}

/**
  *
  */
package object pages {

  private def image(id: String) = http(s"image $id").get(s"/productImage.action?id=$id").headers(acceptImage)
  private def images(id: String*) = id.map(i => image(i))

  val WelcomePage = group("Welcome Page") {
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

  val JuicesPage = group("Juices Page"){
    exec(http("/products_juices.action")
      .get("/products_juices.action")
      .headers(defaultHeader))
  }

  val AccessoiresPage = group("Accessoires Page"){
    exec(http("products_accessoires.action")
      .get("/products_accessoires.action")
      .headers(defaultHeader))
  }

  val BooksPage = group("Books Page") {
    exec(
      http("/products_books.action")
      .get("/products_books.action")
      .headers(defaultHeader)
      .resources(images("7", "8", "9", "10", "11", "12"):_*))
  }

  val CoursesPage = group("Courses Page") {
    exec(
      http("/products_courses.action")
        .get("/products_courses.action")
        .headers(defaultHeader))
  }

}
