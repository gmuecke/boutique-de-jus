package workshop.pages

import java.net.URLEncoder

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._
import scala.language.postfixOps


/**
  * Example for dynamic search query requests. I.e. for real time searches (not implemented in BDJ)
  */
object Search {

  def main(args: Array[String]): Unit = {

    def searchThreshold = 3

    def word = "Quelloffen"

    def searchQueries = Range(searchThreshold, word.length + 1)
      .map(i => word.substring(0, i))

    println(searchQueries)

  }

  def searchRealTime(query: String, paceTime: Duration = 1 second, searchThreshold: Int = 3) = {

    group("Search RealTime") {

      def feederData = Range(searchThreshold, query.length + 1)
        .map(i => query.substring(0, i))
        .map(word => Map("rtquery" -> URLEncoder.encode(word, "UTF-8"))).toList

      group("Search (RealTime)") {

        foreach(feederData, "rtquery", "counter") {
          exec(flattenMapIntoAttributes("${rtquery}"))
            .exec(http("${counter}:query='${rtquery}'")
              .get("/search/?query=${rtquery}"))
            .pace(paceTime)
        }
      }
    }
  }

}
