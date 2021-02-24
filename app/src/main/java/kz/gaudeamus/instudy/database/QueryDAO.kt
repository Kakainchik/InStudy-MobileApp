package kz.gaudeamus.instudy.database

import androidx.room.*
import kz.gaudeamus.instudy.entities.SchoolQuery

@Dao
interface QueryDAO {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertAll(vararg queries: SchoolQuery): LongArray

	@Query("""DELETE FROM queries""")
	suspend fun deleteAll(): Int
}