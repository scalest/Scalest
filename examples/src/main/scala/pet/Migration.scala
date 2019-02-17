package pet

import pet.PetModel.{Pet, Sexes}
import pet.Pets._
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile

import scala.concurrent.Await
import scala.concurrent.duration._

class Migration(dc: DatabaseConfig[H2Profile]) {

  import dc.profile.api._

  def migrate(): Unit = {
    Await.ready(
      dc.db.run(
        DBIO.seq(
          pets.schema.createIfNotExists,
          pets ++= Seq(
            Pet(name = "Garfield", adopted = true, sex = Sexes.Male), Pet(
              name = "Momo",
              adopted = false,
              sex = Sexes.Female
            )
          )
        )
      ), Duration.Inf
    )
  }
}
