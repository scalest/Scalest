package pet

import java.util.UUID

import pet.PetModel.{Pet, Sexes, User}
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile

import scala.concurrent.Await
import scala.concurrent.duration._

class Migrator(dc: DatabaseConfig[H2Profile]) {

  import dc.profile.api._

  def migrate(): Unit = {
    Await.ready(
      dc.db.run(
        DBIO.seq(
          Pets.query.schema.createIfNotExists,
          Pets.query ++= Seq(
            Pet(name = "Garfield", adopted = true, sex = Sexes.Male, bodySize = 24, tags = Seq("pet", "lazy", "fat", "cat")),
            Pet(name = "Momo", adopted = false, sex = Sexes.Female, tags = Seq.fill(15)(UUID.randomUUID().toString), bodySize = 60)
            ),
          Users.query.schema.createIfNotExists,
          Users.query ++= Seq(
            User(username = "Pavel Kravec", password = "1234qwer"),
            User(username = "Pavel Kravec", password = "1234qwer")
            )
          )
        ), Duration.Inf
      )
  }
}
