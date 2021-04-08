package kz.gaudeamus.instudy.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kz.gaudeamus.instudy.DateSerializer
import java.time.LocalDate

/**
 * Сущность карточки.
 * @param cardId ID в локальной базе данных(в приложении). Невидим для сериализатора JSON.
 * @param id ID в серверной базе данных карточек. Видим для JSON.
 */
@Serializable
@Entity(tableName = "cards")
data class Card(
	@Transient
	@ColumnInfo(name = "cardId")
	@PrimaryKey(autoGenerate = true)
	var cardId: Long? = null,
	@Transient
	@ColumnInfo(name = "studentOwnerId")
	val studentId: Long? = null,
	var title: String,
	var content: String?,
	var city: String,
	var faculty: String?,
	var speciality: String?,
	@Serializable(with = DateSerializer::class)
	var created: LocalDate,
	var status: CardStatus,
	@ColumnInfo(name = "id")
	var id: Long? = null) : java.io.Serializable