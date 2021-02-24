package kz.gaudeamus.instudy.models

import io.ktor.client.features.*
import io.ktor.client.features.cookies.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.content.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kz.gaudeamus.instudy.entities.*
import kz.gaudeamus.instudy.models.HttpTask.*
import java.net.URLDecoder

final class AuthorizationRepository : KtorRepository() {

	/**
	 * Ассинхронно делает общий запрос на регистрацию по типу пользователя.
	 */
	private suspend fun makeRegistrationRequest(bodyRequest: String, whom: AccountKind) : HttpTask<RegistrationResponse> {
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

			val body = Json { ignoreUnknownKeys = true }.decodeFromString<RegistrationResponse>(response.readText())

			//Проверяем ответ
			when(response.status) {
				HttpStatusCode.OK -> HttpTask(TaskStatus.COMPLETED, body, WebStatus.NONE)
				else -> HttpTask(TaskStatus.CANCELED, null, WebStatus.NONE)
			}
		}
	}

	/**
	 * Ассинхронно отправляет запрос на регистрацию школы
	 */
	suspend fun makeRegistrationRequest(request: RegistrationSchoolRequest) : HttpTask<RegistrationResponse> {
		return makeRegistrationRequest(Json { encodeDefaults = true }.encodeToString(request), AccountKind.SCHOOL)
	}

	/**
	 * Ассинхронно отправляет запрос на регистрацию студента.
	 */
	suspend fun makeRegistrationRequest(request: RegistrationStudentRequest) : HttpTask<RegistrationResponse> {
		return makeRegistrationRequest(Json { encodeDefaults = true }.encodeToString(request), AccountKind.STUDENT)
	}

	/**
	 * Ассинхронно отправляет запрос на авторизацию пользователя.
	 */
	suspend fun makeAuthorizationRequest(request: AuthorizationRequest) : HttpTask<AuthenticationResponse> {
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

			//Проверяем ответ
			when(response.status) {
				HttpStatusCode.OK -> {
					val body = Json {
						ignoreUnknownKeys = true
					}.decodeFromString<AuthenticationResponse>(response.readText())
					//Берём RefreshToken из куки
					val cookies = httpClient.cookies(AUTHORIZATION_URL)
					body.refreshToken = cookies[REFRESH_TOKEN_COOKIE]?.value

					HttpTask(TaskStatus.COMPLETED, body, WebStatus.NONE)
				}
				else -> HttpTask(TaskStatus.CANCELED, null, WebStatus.NONE)
			}
		}
	}

	/**
	 * Ассинхронно отправляет запрос на отзыв Refresh токена пользователя.
	 * Обычно используется при выходе из аккаунта, дабы по данному токену другие не могли получить новый.
	 */
	suspend fun makeRevokeTokenRequest(refreshToken: String): HttpTask<Nothing> =
		withContext(Dispatchers.IO) {
			val httpClient = initClient()
			val response = httpClient.use {
				it.post<HttpResponse>(REVOKE_TOKEN_URL) {
					timeout {
						requestTimeoutMillis = SHORT_TIMEOUT
						socketTimeoutMillis = SHORT_TIMEOUT
					}

					val json = buildJsonObject { put("token", URLDecoder.decode(refreshToken, "UTF-8")) }
					body = TextContent(Json.encodeToString(json), ContentType.Application.Json)
				}
			}

			//Проверяем ответ
			when(response.status) {
				HttpStatusCode.OK -> HttpTask(TaskStatus.COMPLETED, null, WebStatus.NONE)
				HttpStatusCode.NotFound -> HttpTask(TaskStatus.CANCELED, null, WebStatus.NONE)
				HttpStatusCode.BadRequest -> HttpTask(TaskStatus.CANCELED, null, WebStatus.NONE)
				else -> HttpTask(TaskStatus.CANCELED, null, WebStatus.NONE)
			}
		}

	/**
	 * Ассинхронно отправляет запрос на обновление пароля пользователя.
	 */
	suspend fun makeUpdatePasswordRequest(activeToken: String, request: UpdatePasswordRequest): HttpTask<Nothing> =
		withContext(Dispatchers.IO) {
			val httpClient = initClient()
			val response = httpClient.use {
				it.put<HttpResponse>(UPDATE_PASSWORD_URL) {
					timeout {
						requestTimeoutMillis = SHORT_TIMEOUT
						socketTimeoutMillis = SHORT_TIMEOUT
					}

					header(AUTHORIZATION_HEADER, activeToken)

					val json = Json.encodeToString(request)
					body = TextContent(json, ContentType.Application.Json)
				}
			}

			//Проверяем ответ
			when(response.status) {
				HttpStatusCode.OK -> HttpTask(TaskStatus.COMPLETED, null, WebStatus.NONE)
				HttpStatusCode.Unauthorized -> HttpTask(TaskStatus.CANCELED, null, WebStatus.UNAUTHORIZED)
				HttpStatusCode.BadRequest -> HttpTask(TaskStatus.CANCELED, null, WebStatus.NONE)
				else -> HttpTask(TaskStatus.CANCELED, null, WebStatus.NONE)
			}
		}

	companion object {
		internal const val REGISTRATION_URL = "$HOSTNAME/registration/"
		internal const val AUTHORIZATION_URL = "$HOSTNAME/login/auth"
		internal const val REVOKE_TOKEN_URL = "$HOSTNAME/login/revoke-token"
		internal const val UPDATE_PASSWORD_URL = "$HOSTNAME/login/update-pass"
	}
}