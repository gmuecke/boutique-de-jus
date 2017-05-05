package workshop

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import workshop.pages._

/**
  *
  */
package object scenarios {

  object chains {

    def welcome = exec(WelcomePage)

    def anonymousBrowsing = group("Anonymous Browsing") {
        exec(JuicesPage)
      //EXERCISE add more steps here
    }

    //EXERCISE add more chains here

  }

  val onlyBrowsing = scenario("Only Browser")
    .exec(chains.welcome)
    .exec(chains.anonymousBrowsing)

  //EXERCISE add more scenarios here


}
