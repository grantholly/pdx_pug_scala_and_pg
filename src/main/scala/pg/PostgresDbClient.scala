package pg

import pg.Models.{members,teams}

import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

trait PostgresDbClient {

  val db = Database.forConfig("pg")

  def createInitialSchema(): Unit = {
    val initialMembers = DBIO.seq(
      members += (1, "grant", 1),
      members += (2, "bryant", 1)
    )

    val initialTeams = DBIO.seq(
      teams += (1, "DB"),
      teams += (2, "DDT"),
      teams += (3, "Dirac")
    )

    val schema: Future[Any] = db.run(DBIO.seq(
      teams.schema.createIfNotExists,
      members.schema.createIfNotExists,
      initialMembers,
      initialTeams
    ))
  }

}
