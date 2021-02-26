package kz.gaudeamus.instudy.models

import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.content.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kz.gaudeamus.instudy.entities.AddCardRequest
import kz.gaudeamus.instudy.entities.CardResponse
import kz.gaudeamus.instudy.entities.FilteredCardResponse
import kz.gaudeamus.instudy.models.HttpTask.*

final class CardRepository : KtorRepository() {
	/**
	 * Ассинхронно отправляет запрос на добавление карточки в базу.
	 */
	suspend fun makeAddCardRequest(request: AddCardRequest, activeToken: String): HttpTask<CardResponse> {
		return withContext(Dispatchers.IO) {
			val httpClient = initClient()
			val response = httpClient.use {
				it.post<HttpResponse>(ADD_CARD_URL) {
					timeout {
						requestTimeoutMillis = AVERAGE_TIMEOUT
						socketTimeoutMillis = AVERAGE_TIMEOUT
					}

					header(AUTHORIZATION_HEADER, activeToken)
					val json = Json.encodeToString(request)
					body = TextContent(json, ContentType.Application.Json)
				}
			}

			//Получаем ответ
			when(response.status) {
				HttpStatusCode.OK -> {
					val body = Json {
						ignoreUnknownKeys = true
					}.decodeFromString<CardResponse>(response.readText())
					HttpTask(TaskStatus.COMPLETED, body, WebStatus.NONE)
				}
				HttpStatusCode.Unauthorized -> {
					HttpTask(TaskStatus.CANCELED, null, WebStatus.UNAUTHORIZED)
				}
				else -> HttpTask(TaskStatus.CANCELED, null, WebStatus.NONE)
			}
		}
	}

	/**
	 * Ассинхронно делает запрос на получение всех личных карточек из базы.
	 */
	suspend fun makeGetOwnCardRequest(activeToken: String): HttpTask<Array<CardResponse>> {
		return withContext(Dispatchers.IO) {
			val httpClient = initClient()
			val response = httpClient.use {
				it.get<HttpResponse>(GET_OWN_CARDS_URL) {
					timeout {
						requestTimeoutMillis = AVERAGE_TIMEOUT
						socketTimeoutMillis = AVERAGE_TIMEOUT
					}

					header(AUTHORIZATION_HEADER, activeToken)
				}
			}

			//Получаем ответ
			when(response.status) {
				HttpStatusCode.OK -> {
					val body = Json.decodeFromString<Array<CardResponse>>(response.readText())
					HttpTask(TaskStatus.COMPLETED, body, WebStatus.NONE)
				}
				HttpStatusCode.NotFound -> HttpTask(TaskStatus.COMPLETED, emptyArray<CardResponse>(), WebStatus.NONE)
				HttpStatusCode.Unauthorized -> HttpTask(TaskStatus.CANCELED, null, WebStatus.UNAUTHORIZED)
				else -> HttpTask(TaskStatus.CANCELED, null, WebStatus.NONE)
			}
		}
	}

	/**
	 * Ассинхронно делает запрос на удаление личной карточки из базы.
	 */
	suspend fun makeDeleteCardRequest(activeToken: String, cardId: Long): HttpTask<Boolean> {
		return withContext(Dispatchers.IO) {
			val httpClient = initClient()
			val response = httpClient.use {
				it.delete<HttpResponse>(DELETE_OWN_CARD_URL) {
					timeout {
						requestTimeoutMillis = SHORT_TIMEOUT
						socketTimeoutMillis = SHORT_TIMEOUT
					}

					header(AUTHORIZATION_HEADER, activeToken)
					parameter("id", cardId)
				}
			}

			//Получаем ответ
			when(response.status) {
				HttpStatusCode.OK -> HttpTask(TaskStatus.COMPLETED, null, WebStatus.NONE)
				HttpStatusCode.NotFound -> HttpTask(TaskStatus.CANCELED, null, WebStatus.NONE)
				HttpStatusCode.Unauthorized -> HttpTask(TaskStatus.CANCELED, null, WebStatus.UNAUTHORIZED)
				else -> HttpTask(TaskStatus.CANCELED, null, WebStatus.NONE)
			}
		}
	}

	/**
	 * Ассинхронно делает запрос на получение карточек по фильтру. E.g.: Актуально для школ.
	 */
	suspend fun makeGetAllCardByFilterRequest(activeToken: String): HttpTask<Array<FilteredCardResponse>> {
		return withContext(Dispatchers.IO) {
			val httpClient = initClient()
			val response = httpClient.use {
				it.get<HttpResponse>(GET_CARDS_BY_FILTER) {
					timeout {
						requestTimeoutMillis = LONG_TIMEOUT
						socketTimeoutMillis = LONG_TIMEOUT
					}

					header(AUTHORIZATION_HEADER, activeToken)
				}
			}

			//Получаем ответ
			when(response.status) {
				HttpStatusCode.OK -> {
					val body = Json {
						coerceInputValues = true
					}.decodeFromString<Array<FilteredCardResponse>>(response.readText())
					HttpTask(TaskStatus.COMPLETED, body, WebStatus.NONE)
				}
				HttpStatusCode.NotFound -> HttpTask(TaskStatus.CANCELED, null, WebStatus.NONE)
				HttpStatusCode.Unauthorized -> HttpTask(TaskStatus.CANCELED, null, WebStatus.UNAUTHORIZED)
				else -> HttpTask(TaskStatus.CANCELED, null, WebStatus.NONE)
			}
		}
	}

	companion object {
		internal const val ADD_CARD_URL = "$HOSTNAME/card/add"
		internal const val GET_OWN_CARDS_URL = "$HOSTNAME/card"
		internal const val DELETE_OWN_CARD_URL = "$HOSTNAME/card"
		internal const val GET_CARDS_BY_FILTER = "$HOSTNAME/card/filter"
	}
}