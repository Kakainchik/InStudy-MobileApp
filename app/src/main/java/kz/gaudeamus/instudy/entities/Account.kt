package kz.gaudeamus.instudy.entities

import kotlinx.serialization.Serializable
import kz.gaudeamus.instudy.DateSerializer
import java.time.LocalDate

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
				   val kind: AccountKind) : java.io.Serializable {
	public fun updateToken(newToken: String, newRefreshToken: String): Account =
		this.copy(token = newToken, refreshToken = newRefreshToken)
}