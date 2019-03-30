package pg

import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

object Pg extends App with PostgresDbClient {

  createInitialSchema()

  try {
    val useRows: Future[Seq[(Int, String, Int)]] = db.run(Queries.selectStar)
    useRows.onComplete({
      case Success(res) => {
        println(res.filter(row => row._3 == 1)
          .map(row => row._2.toUpperCase))
      }
      case Failure(e) => e.printStackTrace()
    })
    Await.result(useRows, Duration.Inf)

    val empty: Future[_] = db.run(Queries.upsert)
    empty.onComplete({
      case Success(res) => println(res)
      case Failure(e) => println(e)
    })
    Await.result(empty, Duration.Inf)

    val chain: Future[Any] = {
      val start: Future[Vector[(Int, String, Int)]] = db.run(Queries.selectStar)
      start.onComplete({
        case Success(res) => {
          println("before ETL")
          println(res)
        }
        case Failure(e) =>
          println(e.printStackTrace())
          throw new Exception("cannot complete query")
      })
      Await.result(start, Duration(1000, MILLISECONDS))

      val etl: Future[_] = db.run(DBIO.seq(
        Queries.insert
          .andThen(Queries.update)
          .andFinally(Queries.delete))
      )
      etl.onComplete({
        case Success(res) => res
        case Failure(e) => e.printStackTrace()
      })
      etl
    }
    Await.result(chain, Duration(2000, MILLISECONDS))
  } finally db.close()
}

