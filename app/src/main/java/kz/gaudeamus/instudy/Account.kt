package kz.gaudeamus.instudy

import kotlinx.serialization.Serializable

/**
 *  {
 *      "email": "example@mail.com",
 *      "token": "123abc456",
 *      "kind": "MODERATOR"
 *  }
 */
@Serializable
data class Account(val email: String, val token: String, val kind: AccountKind)