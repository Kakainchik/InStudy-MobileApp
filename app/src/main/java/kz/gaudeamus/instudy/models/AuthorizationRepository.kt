package kz.gaudeamus.instudy.models

import io.ktor.client.features.*
import io.ktor.client.features.cookies.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.content.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kz.gaudeamus.instudy.entities.*

final class AuthorizationRepository : KtorRepository() {

	/**
	 * Ассинхронно делает общий запрос на регистрацию по типу пользователя.
	 */
	private suspend fun makeRegistrationRequest(bodyRequest: String, whom: AccountKind) : Resource<RegistrationResponse> {
		return withContext(Dispatchers.IO) {
			val httpClient = initClient()
			val response = httpClient.use {
				it.post<HttpResponse>("$REGISTRATION_URL${whom.value}") {

					timeout {
						requestTimeoutMillis = AVERAGE_TIMEOUT
						socketTimeoutMillis = AVERAGE_TIMEOUT
					}

					body = TextContent(bodyRequest, ContentType.Application.Json)
				}
			}

			val body = Json {ignoreUnknownKeys = true }.decodeFromString<RegistrationResponse>(response.readText())
			when(response.status) {
				HttpStatusCode.OK -> Resource(Status.COMPLETED, body, null)
				else -> Resource(Status.CANCELED, null, body.message)
			}
		}
	}

	/**
	 * Ассинхронно отправляет запрос на регистрацию школы
	 */
	suspend fun makeRegistrationRequest(request: RegistrationSchoolRequest) : Resource<RegistrationResponse> {
		return makeRegistrationRequest(Json {encodeDefaults = true }.encodeToString(request), AccountKind.SCHOOL)
	}

	/**
	 * Ассинхронно отправляет запрос на регистрацию студента.
	 */
	suspend fun makeRegistrationRequest(request: RegistrationStudentRequest) : Resource<RegistrationResponse> {
		return makeRegistrationRequest(Json {encodeDefaults = true }.encodeToString(request), AccountKind.STUDENT)
	}

	/**
	 * Ассинхронно отправляет запрос на авторизацию пользователя.
	 */
	suspend fun makeAuthorizationRequest(request: AuthorizationRequest) : Resource<AuthenticationResponse> {
		return withContext(Dispatchers.IO) {
			val httpClient = initClient()
			val response = httpClient.use {
				it.post<HttpResponse>(AUTHORIZATION_URL) {

					timeout {
						requestTimeoutMillis = SHORT_TIMEOUT
						socketTimeoutMillis = SHORT_TIMEOUT
					}

					val json = Json.encodeToString(request)
					body = TextContent(json, ContentType.Application.Json)
				}
			}

			when(response.status) {
				HttpStatusCode.OK -> {
					val body = Json {
						ignoreUnknownKeys = true
					}.decodeFromString<AuthenticationResponse>(response.readText())
					//Берём RefreshToken из куки
					val cookies = httpClient.cookies(AUTHORIZATION_URL)
					body.refreshToken = cookies["RefreshToken"]?.value

					Resource(Status.COMPLETED, body, null)
				}
				else -> {
					val json = Json.parseToJsonElement(response.readText())
					val error = json.jsonObject["message"]?.jsonPrimitive?.contentOrNull
					Resource(Status.CANCELED, null, error)
				}
			}
		}
	}

	companion object {
		internal const val REGISTRATION_URL = "$HOSTNAME/api/registration/"
		internal const val AUTHORIZATION_URL = "$HOSTNAME/api/login/auth"
	}
}