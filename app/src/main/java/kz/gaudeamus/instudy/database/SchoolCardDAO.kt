package kz.gaudeamus.instudy.database

import androidx.room.*
import kz.gaudeamus.instudy.entities.Card
import kz.gaudeamus.instudy.entities.FilteredCard
import kz.gaudeamus.instudy.entities.Student

@Dao
interface SchoolCardDAO {
	@Transaction
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertStudent(student: Student): Long

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertCard(card: Card): Long

	@Query("""DELETE FROM cards""")
	suspend fun deleteAllCards()

	@Query("""DELETE FROM students""")
	suspend fun deleteAllStudents()

	@Transaction
	@Query("SELECT * FROM students")
	suspend fun getAll() : List<FilteredCard>
}