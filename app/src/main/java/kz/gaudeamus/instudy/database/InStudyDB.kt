package kz.gaudeamus.instudy.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kz.gaudeamus.instudy.entities.Card
import kz.gaudeamus.instudy.entities.SchoolQuery

/**
 * Контекст локальной базы данных приложения.
 * @param version Важно: инкрементировать каждый раз, когда обновляются сущности базы(таблицы) и миграции.
 */
@Database(entities = [Card::class, SchoolQuery::class], version = 7)
@TypeConverters(Converter::class)
abstract class InStudyDB : RoomDatabase() {

	abstract fun cardDao(): CardDAO
	abstract fun queryDao(): QueryDAO

	internal companion object {
		internal const val DATABASE_NAME = "INSTUDY-DATABASE"
	}
}