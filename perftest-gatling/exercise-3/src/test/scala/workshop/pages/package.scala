package workshop

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder
import workshop.Headers._

/**
  * List of Pages of the WebShop
  */
package object pages {

  /**
    * Shop pages that might be directly accessed
    */
  object ShopPages extends Enumeration {
    class Page(val url:String, val chains: ChainBuilder*)
    val Juices = new Page("http://localhost:8080/products_juices.action", pages.JuicesPage)
    val Accessoires = new Page("http://localhost:8080/products_accessoires.action", pages.AccessoiresPage)
    val Books = new Page("http://localhost:8080/products_books.action", pages.BooksPage)
    val Courses = new Page("http://localhost:8080/products_courses.action", pages.CoursesPage)
  }

  /**
    * Generate a request for a single image
    * @param id
    *   the product id
    * @return
    */
  private def image(id: String) = http(s"image $id").get(s"/productImage.action?id=$id").headers(acceptImage)

  /**
    * Generates a chain of requests for a set of ids
    * @param id
    *  the product id(s)
    * @return
    */
  private def images(id: String*) = id.map(i => image(i))

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

  def JuicesPage = group("Juices Page") {
    exec(http("/products_juices.action")
      .get("/products_juices.action")
      .headers(defaultHeader))
  }

  def AccessoiresPage = group("Accessoires Page") {
    exec(http("products_accessoires.action")
      .get("/products_accessoires.action")
      .headers(defaultHeader))
  }

  def BooksPage = group("Books Page") {
    exec(
      http("/products_books.action")
        .get("/products_books.action")
        .headers(defaultHeader)
        .resources(images("7", "8", "9", "10", "11", "12"): _*))
  }

  def CoursesPage = group("Courses Page") {
    exec(
      http("/products_courses.action")
        .get("/products_courses.action")
        .headers(defaultHeader))
  }

  def RegisterPage = group("Register Page"){
    exec(http("/register_input.action")
      .get("/register_input.action")
      .headers(defaultHeader))
  }

  def SubmitRegistration( username:String,
                          password:String,
                          lastname: String = "TLastname",
                          firstname:String = "TFirstname",
                          email:String = "test@workshop.io",
                          street:String = "Test street",
                          city:String = "Test city",
                          zip:String = "12345",
                          country:String = "ch") =
    group("Submit Registration"){
    exec(http("/register_execute.action (post)")
      .post("/register_execute.action")
      .headers(formHeader)
      .formParam("customer.lastname", lastname)
      .formParam("customer.firstname", firstname)
      .formParam("customer.email", email)
      .formParam("customer.street", street)
      .formParam("customer.city", city)
      .formParam("customer.zip", zip)
      .formParam("customer.country", country)
      .formParam("customer.username", username)
      .formParam("customer.password", password))
  }

  def LoginPage = group("Login Page") {
    exec(http("/Login.action (get)")
      .get("/Login.action")
      .headers(defaultHeader))
  }

  def SubmitLogin = (username: String, password: String) => group("Submit Login") {
    exec(http("/Login.action (post)")
      .post("/Login.action")
      .headers(formHeader)
      .formParam("username", username)
      .formParam("password", password))
  }

  def Logout = group("Logout") {
    exec(http("/Logout.action")
      .get("/Logout.action")
      .headers(defaultHeader))
  }

  def AddToCart = (productId: String, quantity: Int, page: ShopPages.Page) => group("Add to Cart") {
    exec(http("/cart_add.action (post)")
      .post("/cart_add.action")
      .headers(formHeader)
      .formParam("url", page.url)
      .formParam("quantity", quantity)
      .formParam("id", productId))
  }

  def ShowCart(products:String*) = group("Show Cart") {
    exec(http("/cart_.action")
      .get("/cart_.action")
      .headers(defaultHeader)
      .resources(images(products:_*): _*))
  }

  def ShowOrderSummary(products:String*) = group("Order Summary") {
    exec(http("/secure/order_input.action")
      .get("/secure/order_input.action")
      .headers(defaultHeader)
      .resources(images(products:_*): _*))
  }

  def SubmitOrder = group("Order Submit") {
    exec(http("/secure/order_execute.action")
      .post("/secure/order_execute.action")
      .headers(formHeader)
      .formParam("order", "Submit Order"))
  }
}
