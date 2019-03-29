package pg

import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

object Pg extends App with PostgresDbClient {

  //val db = Database.forConfig("docs")

  class Teams(tag: Tag) extends Table[(Int, String)](tag, "teams") {
    def id = column[Int]("id", O.PrimaryKey)
    def name = column[String]("name")
    def * = (id, name)
  }

  val teams = TableQuery[Teams]

  class Members(tag: Tag) extends Table[(Int, String, Int)](tag, "members") {
    def id = column[Int]("id", O.PrimaryKey)
    def team_id = column[Int]("team_id")
    def team_id_fk = foreignKey("fk_teams", id, teams)(r => r.id, onUpdate=ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
    def name = column[String]("name")
    def * = (id, name, team_id)
  }

  val members = TableQuery[Members]

  val initalMembers = DBIO.seq(
    members += (1, "grant", 1),
    members += (2, "bryant", 1)
  )

  val initialTeams = DBIO.seq(
    teams += (1, "DB"),
    teams += (2, "DDT"),
    teams += (3, "Dirac")
  )

  val schema = db.run(DBIO.seq(
    teams.schema.createIfNotExists,
    members.schema.createIfNotExists,
    initalMembers,
    initialTeams
  ))

  try {
    val q = sql"select 'yup' as string;".as[String]
    val q2 = sql"select 1;".as[Int]
    val q3 = sql"select * from members;".as[(Int, String)]
    // select .filterOpt .filterIf
    val q4 = members.filter(m => m.id > 1).map(m => (m.id, m.name)).result
    val filt = members.filterIf(true)(r => r.id > 1)
    // can filter on options without unwrapping
    val ofilt = members.filterOpt(Option(1))(_.id > _)
    //aggregate .min .max .avg .length count() .exists
    val ids = members.map(_.id)
    val q5 = ids.sum.result
    // joins
    // cartesian product warning !!!
    // val ij = members.join(teams).result
    //println(ij.statements.head)
    // also joinLeft, joinRight, joinFull
    // need to handle nulls with .map when we yield in outer and full joins
    val ij = (for {
      (m, t) <- members.join(teams).on((m, t) => m.team_id === t.id)
    } yield (m.name, t.name)).result
    val oj = (for {
      (m, t) <- members.joinRight(teams).on((m, t) => m.team_id === t.id)
    } yield (m.map(m => m.name), t.name)).result
    // unions intersect and except must be done with scala methods
    val u = members.map(m => (m.id, m.name)).unionAll(teams)
    //group by
    val q6 = members.groupBy(g => g.name).map{
      case (name, group) => (name, group.map(_.id).sum)
    }
    // having
    val hav = q6.filter{
      case (name, count) => count > 1
    }.map(_._2).result
    // insert
    val q7 = members += (3, "russ", 1)
    // update
    val q8 = members.filter(m => m.id === 1).update(3, "john", 1)
    // delete
    val q9 = members.filter(m => m.id === 2).delete
    // sort by
    val q10 = members.sortBy(m => m.name)
    // windowing
    val q11 = members.drop(2).take(1)
    val q12 = sql"""select * from members;""".as[(Int, String)]
    // upsert
    val q13 = members.insertOrUpdate((4, "Zoiks", 1))
    val q14 =
      sql"""select
           t.name
           from members m right outer join teams t
           on m.team_id = t.id
           where m.name is null;
         """.as[String]
    // transaction control
    val q15 = for {
      _ <- DBIO.seq(
        members += (10, "Jack", 2),
        members += (11, "Tony", 2),
        members.filter(m => m.id >= 10).delete,
        members.filter(m => m.team_id === 1).update(1, "grant", 10)
      )
    } yield DBIO.failed(new Exception("Rolling back")).transactionally

    // don't need to unpack the future Any
    val done: Future[_] = db.run(q10.result)

    done.onComplete({
      case Success(res) => {
        println("from the Future[Any]")
        println(res)
      }
      case Failure(e) => println("failed!")
    })

    // want to unpack and use rows
    val jj: Future[Seq[(String)]] = db.run(q14)
    jj.onComplete({
      case Success(res) => {
        println("from the unpacked Future")
        // can unpack the Option[]
        // println(res.headOption.getOrElse("oh no"))
        println(res.headOption.get.toUpperCase())
      }
      case Failure(e) => println(e)
    })

    // running a batch
    /*
    val jjj: Future[_] = db.run(DBIO.seq(
      q13.andThen(
        q14.andThen(q12)
          .map(r => r.foreach(s => println(s)))
      )
    ))
*/
    Await.result(jj, Duration.Inf)
    println("running batch")
    //Await.result(jjj, Duration.Inf)
    Await.result(done, Duration.Inf)
  } finally db.close()
}

