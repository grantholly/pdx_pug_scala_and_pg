package pg

import slick.jdbc.PostgresProfile.api._

object Models {

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

}
