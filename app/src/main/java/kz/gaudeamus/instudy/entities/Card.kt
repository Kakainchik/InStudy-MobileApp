package kz.gaudeamus.instudy.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kz.gaudeamus.instudy.DateSerializer
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Сущность карточки.
 * @param guid ID в локальной базе данных(в приложении). Невидим для сериализатора JSON.
 * @param id ID в серверной базе данных карточек. Видим для JSON.
 */
@Serializable
@Entity(tableName = "cards")
data class Card(
	@Transient
	@ColumnInfo(name = "guid")
	@PrimaryKey(autoGenerate = true)
	var guid: Long? = null,
	var title: String,
	var content: String,
	var city: String,
	var faculty: String,
	var speciality: String,
	@Serializable(with = DateSerializer::class)
	var created: LocalDate,
	var status: CardStatus,
	@ColumnInfo(name = "id")
	var id: Long? = null) : java.io.Serializable