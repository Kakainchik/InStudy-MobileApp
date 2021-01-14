package kz.gaudeamus.instudy.entities

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Serializable
data class Card(var title: String,
				var content: String,
				var city: String,
				@Serializable(with = DateSerializer::class)
				val created: LocalDate,
				var status: CardStatus,
				var id: Int? = null) {

	private object DateSerializer : KSerializer<LocalDate> {

		override fun serialize(encoder: Encoder, value: LocalDate) {
			val string = value.format(DateTimeFormatter.ISO_LOCAL_DATE)
			encoder.encodeString(string)
		}

		override fun deserialize(decoder: Decoder): LocalDate {
			val string = decoder.decodeString()
			return LocalDate.parse(string, DateTimeFormatter.ISO_LOCAL_DATE)
		}

		override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)
	}
}