package kz.gaudeamus.instudy.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
	@PrimaryKey(autoGenerate = true)
	@ColumnInfo(name = "studentId")
	var studentId: Long? = null,
	val email: String,
	val phone: String,
	val name: String,
	val surname: String?) : java.io.Serializable