package pg

import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
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

    val chain: Future[_] = db.run(
      DBIO.seq(
        Queries.insert
          .andThen(Queries.update)
          .andFinally(Queries.delete)
      )
    )
    Await(chain, Duration(1, "second"))

  } finally db.close()
}

