package kz.gaudeamus.instudy.entities

import androidx.room.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kz.gaudeamus.instudy.DateSerializer
import java.time.LocalDate
import java.util.stream.Collectors

/**
 * Сущность запроса на регистрации школы в локальной базе данных.
 */
@Serializable
@Entity(tableName = "queries")
data class SchoolQuery(
	@Transient
	@ColumnInfo(name = "guid")
	@PrimaryKey(autoGenerate = true)
	val guid: Long? = null,
	@ColumnInfo(name = "id")
	val id: Long,
	val email: String,
	@Serializable(with = DateSerializer::class)
	val created: LocalDate,
	val isVerified: Boolean,
	val organization: String,
	var props: List<String> = emptyList()) : java.io.Serializable