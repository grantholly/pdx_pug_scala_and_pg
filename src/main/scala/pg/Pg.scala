package pg

import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}


object SelectOne extends App {

  val db = Database.forConfig("docs")

  try {
    val q = sql"select 'yup' as string;".as[String]
    val q2 = sql"select 1;".as[Int]

    val done: Future[_] = db.run(q2)
    done.onComplete({
      case Success(res) => {
        println(res)
      }
      case Failure(e) => println("failed!")
    })
    Await.result(done, Duration.Inf)
  } finally db.close()
}

