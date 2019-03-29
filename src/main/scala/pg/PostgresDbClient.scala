package pg

import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait PostgresDbClient {

  val db = Database.forConfig("pg")

  def dbNoReturn[Q](q: String): Unit = {

  }

  def dbReturn(q: String): Unit = {

  }

}
