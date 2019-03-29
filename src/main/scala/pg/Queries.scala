package pg

import pg.Models.{members,teams}

import slick.jdbc.PostgresProfile.api._

object Queries {

  // scalars
  val selectYup = sql"select 'yup' as string;".as[String]
  val selectOne = sql"select 1;".as[Int]
  val selectStar = sql"select * from members;".as[(Int, String, Int)]

  // filtering
  val basicWhere = members.filter(m => m.id > 1).map(m => (m.id, m.name)).result
  val ifFilter = members.filterIf(true)(r => r.id > 1).result
  // can filter on options without unwrapping
  val optionFilter = members.filterOpt(Option(1))(_.id > _).result

  //aggregate .min .max .avg .length count() .exists
  val allIds = members.map(_.id)
  val sumOfIds = allIds.sum.result
  val minId = allIds.min.result
  val avgId = allIds.avg.result
  val maxId = allIds.max.result
  val countIds = allIds.length.result

  // joins
  // cartesian product warning !!!
  // val ij = members.join(teams).result
  // println(ij.statements.head)
  // also joinLeft, joinRight, joinFull
  // need to handle nulls with .map when we yield in outer and full joins
  val innerJoin = (for {
    (m, t) <- members.join(teams).on((m, t) => m.team_id === t.id)
  } yield (m.name, t.name)).result
  val outerJoin = (for {
    (m, t) <- members.joinRight(teams).on((m, t) => m.team_id === t.id)
  } yield (m.map(m => m.name), t.name)).result

  // unions intersect and except must be done with scala methods
  val unions = members.map(m => (m.id, m.name)).unionAll(teams).result

  //group by
  val groupByClause = members.groupBy(g => g.name).map{
    case (name, group) => (name, group.map(g => g.id).sum)
  }
  // having
  val groupByHaving = groupByClause.filter{
    case (name, count) => count > 1
  }.map(g => g._2).result

  // insert
  val insert = members += (5, "russ", 1)
  // update
  val update = members.filter(m => m.id === 1).update(6, "john", 1)
  // delete
  val delete = members.filter(m => m.id === 2).delete

  // sort by
  val orderBy = members.sortBy(m => m.name).result

  // windowing
  val paging = members.drop(2).take(1).result

  // upsert
  val upsert = members.insertOrUpdate((4, "Zoiks", 1))

  // raw SQL
  val rawSql =
    sql"""select
           t.name
           from members m right outer join teams t
           on m.team_id = t.id
           where m.name is null;
         """.as[String]

  // transaction control
  val transactionControl = DBIO.seq(
    // add a bonus member.  Cool!
    members += (11, "Janice", 10),
    // delete everyone by getting the aligator wrong.  Whoops!
    members.filter(m => m.id > 0).delete,
    // this fails and rolls back everything
    // undoes the delete and bonus member
    members.filter(m => m.team_id === 1).update(1, "grant", 10)
  ).transactionally
  
}
