package kz.gaudeamus.instudy.entities

import kotlinx.serialization.Serializable

/**
 * @see
 *  {
 *  	"id": 1
 *      "email": "example@mail.com",
 *      "token": "Afhtg235h1az",
 *      "kind": "31"
 *  }
 */
@Serializable
data class Account(val id: Int,
				   val email: String,
				   val token: String,
				   val refreshToken: String,
				   val kind: AccountKind) : java.io.Serializable