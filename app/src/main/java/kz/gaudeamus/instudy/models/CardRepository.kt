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
import kz.gaudeamus.instudy.entities.AddCardResponse

final class CardRepository : KtorRepository() {
	suspend fun makeAddCardRequest(request: AddCardRequest, activeToken: String): Resource<AddCardResponse> {
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

			when(response.status) {
				HttpStatusCode.OK -> {
					val body = Json {
						ignoreUnknownKeys = true
					}.decodeFromString<AddCardResponse>(response.readText())
					Resource(Status.COMPLETED, body, null)
				}
				HttpStatusCode.Unauthorized -> {
					Resource(Status.CANCELED, null, "Unauthorized")
				}
				else -> Resource(Status.CANCELED, null, null)
			}
		}
	}

	companion object {
		internal const val ADD_CARD_URL = "$HOSTNAME/api/card/add"
	}
}