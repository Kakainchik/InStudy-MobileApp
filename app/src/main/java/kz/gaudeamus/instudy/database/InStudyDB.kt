package kz.gaudeamus.instudy.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kz.gaudeamus.instudy.entities.Card

/**
 * Контекст локальной базы данных приложения.
 * @param version Важно: инкрементировать каждый раз, когда обновляются сущности базы(таблицы) и миграции.
 */
@Database(entities = arrayOf(Card::class), version = 6)
@TypeConverters(Converter::class)
abstract class InStudyDB : RoomDatabase() {

	abstract fun cardDao(): CardDAO
}