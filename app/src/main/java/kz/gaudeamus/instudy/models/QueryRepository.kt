package kz.gaudeamus.instudy.models

import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kz.gaudeamus.instudy.entities.PropsResponse
import kz.gaudeamus.instudy.entities.SchoolQuery
import kz.gaudeamus.instudy.models.HttpTask.*
import kotlin.io.use

class QueryRepository : KtorRepository() {
	/**
	 * Асинхронно делает запрос на получение запросов школ на их регистрацию.
	 */
	public suspend fun makeGetUnverifiedSchoolsRequest(activeToken: String): HttpTask<Array<SchoolQuery>> =
		withContext(Dispatchers.IO) {
			val httpClient = initClient()
			val response: HttpResponse = httpClient.use {
				it.get(UNVERIFIED_SCHOOLS_URL) {
					timeout {
						requestTimeoutMillis = AVERAGE_TIMEOUT
						socketTimeoutMillis = AVERAGE_TIMEOUT
					}

					header(AUTHORIZATION_HEADER, activeToken)
				}
			}

			when(response.status) {
				HttpStatusCode.OK -> {
					val body = Json {
						ignoreUnknownKeys = true
					}.decodeFromString<Array<SchoolQuery>>(response.readText())

					HttpTask(TaskStatus.COMPLETED, body, WebStatus.NONE)
				}
				HttpStatusCode.Unauthorized -> HttpTask(TaskStatus.CANCELED, null, WebStatus.UNAUTHORIZED)
				else -> HttpTask(TaskStatus.CANCELED, null, WebStatus.NONE)
			}
		}

	/**
	 * Асинхронно делает запрос получение реквизитов школ.
	 */
	public suspend fun makeGetPropsRequest(activeToken: String, id: Long): HttpTask<Array<PropsResponse>> =
		withContext(Dispatchers.IO) {
			val httpClient = initClient()
			val response: HttpResponse = httpClient.use {
				it.get(PROPS_URL) {
					timeout {
						requestTimeoutMillis = AVERAGE_TIMEOUT
						socketTimeoutMillis = AVERAGE_TIMEOUT
					}

					header(AUTHORIZATION_HEADER, activeToken)

					parameter("id", id)
				}
			}

			when(response.status) {
				HttpStatusCode.OK -> {
					val body = Json.decodeFromString<Array<PropsResponse>>(response.readText())
					HttpTask(TaskStatus.COMPLETED, body, WebStatus.NONE)
				}
				HttpStatusCode.Unauthorized -> HttpTask(TaskStatus.CANCELED, null, WebStatus.UNAUTHORIZED)
				else -> HttpTask(TaskStatus.CANCELED, null, WebStatus.NONE)
			}
		}

	/**
	 * Асинхронно получает запрос на верификацию школы.
	 */
	public suspend fun makeVerifyQueryRequest(activeToken: String, id: Long): HttpTask<Boolean> =
		withContext(Dispatchers.IO) {
			val httpClient = initClient()
			val response: HttpResponse = httpClient.use {
				it.put(VERIFY_SCHOOL_URL) {
					timeout {
						requestTimeoutMillis = AVERAGE_TIMEOUT
						socketTimeoutMillis = AVERAGE_TIMEOUT
					}

					header(AUTHORIZATION_HEADER, activeToken)

					parameter("id", id)
				}
			}

			when(response.status) {
				HttpStatusCode.OK -> HttpTask(TaskStatus.COMPLETED, true, WebStatus.NONE)
				HttpStatusCode.Unauthorized -> HttpTask(TaskStatus.CANCELED, null, WebStatus.UNAUTHORIZED)
				HttpStatusCode.NotFound -> HttpTask(TaskStatus.CANCELED, false, WebStatus.NONE)
				else -> HttpTask(TaskStatus.CANCELED, null, WebStatus.NONE)
			}
		}

	/**
	 * Асинхронно делает запрос на отклонение запроса школы.
	 */
	public suspend fun makeDenyQueryRequest(activeToken: String, id: Long, comment: String? = null): HttpTask<Boolean> =
		withContext(Dispatchers.IO) {
			val httpClient = initClient()
			val response: HttpResponse = httpClient.use {
				it.delete(VERIFY_SCHOOL_URL) {
					timeout {
						requestTimeoutMillis = AVERAGE_TIMEOUT
						socketTimeoutMillis = AVERAGE_TIMEOUT
					}

					header(AUTHORIZATION_HEADER, activeToken)

					parameter("id", id)
					parameter("cause", comment)
				}
			}

			when(response.status) {
				HttpStatusCode.OK -> HttpTask(TaskStatus.COMPLETED, true, WebStatus.NONE)
				HttpStatusCode.Unauthorized -> HttpTask(TaskStatus.CANCELED, null, WebStatus.UNAUTHORIZED)
				HttpStatusCode.NotFound -> HttpTask(TaskStatus.CANCELED, false, WebStatus.NONE)
				else -> HttpTask(TaskStatus.CANCELED, null, WebStatus.NONE)
			}
		}

	companion object {
		internal const val UNVERIFIED_SCHOOLS_URL = "$HOSTNAME/registration"
		internal const val PROPS_URL = "$UNVERIFIED_SCHOOLS_URL/props"
		internal const val VERIFY_SCHOOL_URL = "$UNVERIFIED_SCHOOLS_URL/verify-school"
	}
}