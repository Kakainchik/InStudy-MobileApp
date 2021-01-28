package kz.gaudeamus.instudy.entities

import kotlinx.serialization.Serializable

/**
 * Модель ответа на регистрацию пользователя.
 * @see
 * 	{
 * 		"message": "text"
 * 	}
 */
@Serializable
data class RegistrationResponse(val message: String?)

/**
 * Модель ответа на аутентификацию пользователя.
 * @see
 * 	{
 * 		"id": 1,
 * 		"email": "test@mail.com",
 * 		"token": "Afhtg235h1az",
 * 		"role": 31,
 * 		"isVerified": false,
 * 		"refreshToken": "zs1h532gthfA"
 * 	}
 */
@Serializable
data class AuthenticationResponse(val id: Int,
								  val email: String,
								  val token: String,
								  val role: Int,
								  val isVerified: Boolean,
								  var refreshToken: String? = null)

@Serializable
data class AddCardResponse(val id: Long,
						   val title: String,
						   val content: String,
						   val soughtCity: String,
						   val faculty: String,
						   val speciality: String,
						   val created: String,
						   val isValid: Boolean)