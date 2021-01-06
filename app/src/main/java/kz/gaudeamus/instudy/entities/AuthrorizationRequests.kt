package kz.gaudeamus.instudy.entities

import kotlinx.serialization.Serializable


/**
 * Модель запроса на регистрацию школы
 * @see
 *  {
 *      "email": "example@mail.com",
 *      "password": "123abc456",
 *      "kind": 50,
 *      "organization": "sampleOrg",
 *      "props": `[{file1}, {file2}]`
 *  }
 */
@Serializable
data class RegistrationSchoolRequest(val email: String,
									 val password: String,
									 val role: Int = AccountKind.SCHOOL.value,
									 val organization: String,
									 val props: Array<String>?)

/**
 * Модель запроса на регистрацию студента.
 *  @see
 *  {
 *      "email": "test@mail.com",
 *		"password": "sample12GGG",
 *		"kind": 31,
 *		"phone": "+77771111111",
 *		"name": "Alice",
 *		"surname": null
 *  }
 */
@Serializable
data class RegistrationStudentRequest(val email: String,
									  val password: String,
									  val role: Int = AccountKind.STUDENT.value,
									  val phone: String,
									  val name: String,
									  val surname: String? = null)

/**
 * Модель запроса на авторизацию пользователя
 * 	@see
 * 	{
 * 		"email": "test@mail.com",
 * 		"password": "sample12GGG"
 * 	}
 */
@Serializable
data class AuthorizationRequest(val email: String,
								val password: String)