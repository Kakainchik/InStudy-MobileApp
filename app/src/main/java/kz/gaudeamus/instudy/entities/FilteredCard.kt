package kz.gaudeamus.instudy.entities

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Сущность карточки для школы.
 */
data class FilteredCard(
	@Embedded
	val student: Student,
	@Relation(parentColumn = "studentId", entityColumn = "studentOwnerId")
	val card: Card) : java.io.Serializable