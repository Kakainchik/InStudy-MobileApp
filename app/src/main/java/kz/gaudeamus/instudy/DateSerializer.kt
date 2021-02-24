package kz.gaudeamus.instudy

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Сериализатор для перевода даты в текстовый формат ISO и наоборот.
 */
object DateSerializer : KSerializer<LocalDate> {

	override fun serialize(encoder: Encoder, value: LocalDate) {
		val string = value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
		encoder.encodeString(string)
	}

	override fun deserialize(decoder: Decoder): LocalDate {
		val string = decoder.decodeString()
		return LocalDate.parse(string, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
	}

	override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)
}