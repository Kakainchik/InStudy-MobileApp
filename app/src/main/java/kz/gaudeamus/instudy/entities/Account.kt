package kz.gaudeamus.instudy.entities

import kotlinx.serialization.Serializable

/**
 *  {
 *      "email": "example@mail.com",
 *      "token": "Afhtg235h1az",
 *      "kind": "31"
 *  }
 */
@Serializable
data class Account(val email: String, val token: String, val kind: AccountKind)