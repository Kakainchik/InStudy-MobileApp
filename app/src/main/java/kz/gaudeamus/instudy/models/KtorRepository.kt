package kz.gaudeamus.instudy.models

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.cookies.*
import io.ktor.client.request.*

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
			install(HttpCookies)
			engine {
				config {
					retryOnConnectionFailure(true)
				}
			}
		}

	companion object {
		internal const val HOSTNAME = "http://95.59.10.62:44338"

		internal const val CONTENT_TYPE = "application/json"
		internal const val ACCEPT = CONTENT_TYPE
		internal const val CONNECTION = "keep-alive"

		internal const val SHORT_TIMEOUT = 5_000L
		internal const val AVERAGE_TIMEOUT = 15_000L
		internal const val LONG_TIMEOUT = 30_000L

		internal const val AUTHORIZATION_HEADER = "Authorization"
	}
}