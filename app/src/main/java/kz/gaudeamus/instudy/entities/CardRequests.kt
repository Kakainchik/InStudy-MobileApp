package kz.gaudeamus.instudy.entities

import kotlinx.serialization.Serializable

@Serializable
data class AddCardRequest(val title: String,
						  val content: String,
						  val soughtCity: String,
						  val faculty: String,
						  val speciality: String)

@Serializable
data class UpdateCardRequest(val id: Long,
							 val title: String?,
							 val content: String?,
							 val soughtCity: String?,
							 val faculty: String?,
							 val speciality: String?)