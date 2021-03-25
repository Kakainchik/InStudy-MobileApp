package kz.gaudeamus.instudy.database

import androidx.room.*
import kz.gaudeamus.instudy.entities.Card
import kz.gaudeamus.instudy.entities.SchoolQuery
import kz.gaudeamus.instudy.entities.Student

/**
 * Контекст локальной базы данных приложения.
 * @param version Важно: инкрементировать каждый раз, когда обновляются сущности базы(таблицы) и миграции.
 */
@Database(entities = [Card::class, SchoolQuery::class, Student::class], version = 12)
@TypeConverters(Converter::class)
abstract class InStudyDB : RoomDatabase() {

	abstract fun cardDao(): StudentCardDAO
	abstract fun schoolCardDao(): SchoolCardDAO
	abstract fun queryDao(): QueryDAO

	internal companion object {
		internal const val DATABASE_NAME = "INSTUDY-DATABASE"
	}
}