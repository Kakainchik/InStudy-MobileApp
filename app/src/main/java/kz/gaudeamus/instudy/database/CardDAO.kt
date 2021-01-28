package kz.gaudeamus.instudy.database

import androidx.room.*
import kz.gaudeamus.instudy.entities.Card

@Dao
interface CardDAO {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertAll(vararg cards: Card): LongArray

	@Update
	suspend fun update(vararg cards: Card)

	@Delete
	suspend fun delete(card: Card)

	@Query("SELECT * FROM cards")
	suspend fun getAll() : List<Card>

	@Query("SELECT * FROM cards WHERE id = :id LIMIT 1")
	suspend fun getById(id: Int) : Card
}