package kz.gaudeamus.instudy.models

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.cookies.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kz.gaudeamus.instudy.entities.AuthenticationResponse
import kz.gaudeamus.instudy.entities.RefreshTokenResponse
import kz.gaudeamus.instudy.models.HttpTask.*
import java.lang.Exception
import java.net.URLDecoder

abstract class KtorRepository {
	//Используем отдельный клиент для каждого метода, так как ktor забагован для использования async
	protected fun initClient(): HttpClient =
		HttpClient(OkHttp) {
			//Ожидаем, что будем получать не только HTTP200
			expectSuccess = false
			//Устанавливаем настройки по умолчанию для запросов
			install(DefaultRequest) {
				headers {
					append("Accept", ACCEPT)
					append("Connection", CONNECTION)
				}
			}
			install(HttpTimeout)
			install(HttpCookies) {
				storage = AcceptAllCookiesStorage()
			}
			engine {
				config {
					retryOnConnectionFailure(true)
				}
			}
		}

	/**
	 * Ассинхронно посылает POST запрос на получение нового токена по имеющемуся Refresh токену.
	 */
	public open suspend fun makeRefreshTokenRequest(refreshToken: String): HttpTask<RefreshTokenResponse> =
		withContext(Dispatchers.IO) {
			val httpClient: HttpClient = initClient()
			val response = httpClient.use {
				it.post<HttpResponse>(REFRESH_TOKEN_URL) {
					timeout {
						requestTimeoutMillis = AVERAGE_TIMEOUT
						socketTimeoutMillis = AVERAGE_TIMEOUT
					}

					header(REFRESH_TOKEN_COOKIE, URLDecoder.decode(refreshToken, "UTF-8"))
				}
			}

			when(response.status) {
				HttpStatusCode.OK -> {
					val body = Json {
						ignoreUnknownKeys = true
					}.decodeFromString<RefreshTokenResponse>(response.readText())
					//Берём RefreshToken из куки
					val cookies = httpClient.cookies(AuthorizationRepository.AUTHORIZATION_URL)
					body.refreshToken = cookies[REFRESH_TOKEN_COOKIE]?.value

					HttpTask(TaskStatus.COMPLETED, body, WebStatus.NONE)
				}
				else -> HttpTask(TaskStatus.CANCELED, null, WebStatus.UNAUTHORIZED)
			}
	}

	/**
	 * Производит запрос, переданный через параметр, с обрабаткой на соединение с интернетом.
	 * Чтобы не обрабатывать каждый раз запросы на ошибки в соединении, желательно вызывать их через этот метод.
	 */
	public suspend inline fun <RES> makeRequest(function: () -> HttpTask<RES>): HttpTask<RES> =
		try {
			function.invoke()
		} catch(ex: ConnectTimeoutException) {
			HttpTask(TaskStatus.CANCELED, null, WebStatus.TIMEOUT)
		} catch(ex: HttpRequestTimeoutException) {
			HttpTask(TaskStatus.CANCELED, null, WebStatus.TIMEOUT)
		} catch(ex: Exception) {
			HttpTask(TaskStatus.CANCELED, null, WebStatus.UNABLE_CONNECT)
		}

	companion object {
		internal const val HOSTNAME = "http://5.76.202.190:44338/api"
		internal const val REFRESH_TOKEN_URL = "$HOSTNAME/login/refresh-token"

		internal const val CONTENT_TYPE = "application/json"
		internal const val ACCEPT = CONTENT_TYPE
		internal const val CONNECTION = "keep-alive"

		internal const val SHORT_TIMEOUT = 5_000L
		internal const val AVERAGE_TIMEOUT = 15_000L
		internal const val LONG_TIMEOUT = 30_000L

		internal const val AUTHORIZATION_HEADER = "Authorization"

		internal const val REFRESH_TOKEN_COOKIE = "RefreshToken"
	}
}