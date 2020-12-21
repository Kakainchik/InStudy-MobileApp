package kz.gaudeamus.instudy.entities

import kotlinx.serialization.Serializable


/**
 * @example
 *  {
 *      "email": "example@mail.com",
 *      "password": "123abc456",
 *      "role": 50,
 *      "organization": "sampleOrg",
 *      "props": `[{file1}, {file2}]`
 *  }
 */
@Serializable
data class RegistrationSchool(val email: String,
							  val password: String,
							  val role: AccountKind = AccountKind.SCHOOL,
							  val organization: String,
							  val props: Array<String>?)

/**
 *  @example
 *  {
 *      "email": "test@mail.com",
 *		"password": "sample12GGG",
 *		"role": 31,
 *		"phone": "+77771111111",
 *		"name": "Alice",
 *		"surname": null
 *  }
 */
@Serializable
data class RegistrationStudent(val email: String,
							   val password: String,
							   val role: AccountKind = AccountKind.STUDENT,
							   val phone: String,
							   val name: String,
							   val surname: String? = null)