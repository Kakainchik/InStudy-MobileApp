package kz.gaudeamus.instudy.database

import androidx.room.TypeConverter
import kz.gaudeamus.instudy.entities.CardStatus
import java.time.LocalDate
import java.util.stream.Collectors

/**
 * Конвертер для преобразования данных из SQLite в java объекты.
 */
class Converter {
	@TypeConverter
	fun fromLocalDate(date: LocalDate) : Long = date.toEpochDay()

	@TypeConverter
	fun toLocalDate(value: Long) : LocalDate = LocalDate.ofEpochDay(value)

	@TypeConverter
	fun fromCardStatus(status: CardStatus) : String = status.name

	@TypeConverter
	fun toCardStatus(name: String) : CardStatus = CardStatus.valueOf(name)

	@TypeConverter
	fun fromProps(props: List<String>): String = props.stream().collect(Collectors.joining(","))

	@TypeConverter
	fun toProps(data: String): List<String> = data.split(",")
}