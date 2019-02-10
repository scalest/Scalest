package pet

import java.util.UUID

import pet.PetModel.{Genders, Pet}
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random

object Migration {
  def randomEnum[E <: Enumeration](enum: E): E#Value = enum.values.toList.apply(Random.nextInt(enum.values.size - 1))

  def randomList[T](size: Int, factory: () => T): Seq[T] = Seq.fill(size)(factory())

  def randomPet(): Pet =
    Pet(
      id = UUID.randomUUID.toString,
      name = Random.nextString(10),
      adopted = Random.nextBoolean(),
      gender = randomEnum(Genders),
      bodySize = Random.nextInt(255).toByte,
      tags = Seq.fill(Random.nextInt(23))(Random.nextString(5)),
    )

  def migrate(implicit dc: DatabaseConfig[H2Profile]): Unit = {
    import dc.profile.api._

    Await.ready(
      dc.db.run(
        DBIO.seq(
          Pets.pets.schema.createIfNotExists,
          Pets.pets ++= randomList(50, randomPet),
        ),
      ),
      Duration.Inf,
    )
  }
}
