package kz.gaudeamus.instudy.entities

import kotlinx.serialization.Serializable

@Serializable
data class SchoolRegistrationResponse(val id: Int,
									  val email: String,
									  val created: String,
									  val isVerified: Boolean,
									  val organization: String)