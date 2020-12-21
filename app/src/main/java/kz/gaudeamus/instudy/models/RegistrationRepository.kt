package kz.gaudeamus.instudy.models

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.content.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kz.gaudeamus.instudy.entities.AccountKind
import kz.gaudeamus.instudy.entities.RegistrationSchool

private const val REGISTRATION_URL = "http://95.59.10.62:44338/api/registration/"

private const val CONTENT_TYPE = "application/json"
private const val ACCEPT = CONTENT_TYPE
private const val CONNECTION = "keep-alive"

final class RegistrationRepository {
	private val header = mapOf<String, String>(
		"Content-Type" to CONTENT_TYPE,
		"Accept" to ACCEPT,
		"Connection" to CONNECTION)

	//Отправляем запрос на регистрацию школы
	suspend fun makeRegistrationSchoolRequest(request: RegistrationSchool) {
		return withContext(Dispatchers.IO) {
			val httpClient = HttpClient()
			var response =
					httpClient.post<HttpResponse>("$REGISTRATION_URL${AccountKind.SCHOOL.value}") {
						headers {
							append("Accept", ACCEPT)
							append("Connection", CONNECTION)
						}

						body = TextContent(
							Json.encodeToString(request),
							ContentType.Application.Json)
					}

			val code = response.status
			httpClient.close()
		}
	}
}