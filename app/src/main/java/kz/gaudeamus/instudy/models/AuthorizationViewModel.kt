package kz.gaudeamus.instudy.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.features.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kz.gaudeamus.instudy.R
import kz.gaudeamus.instudy.SingleLiveEvent
import kz.gaudeamus.instudy.entities.AuthenticationResponse
import kz.gaudeamus.instudy.entities.AuthorizationRequest
import java.lang.Exception

class AuthorizationViewModel(application: Application) : AndroidViewModel(application) {
	public val signinLiveData = SingleLiveEvent<Resource<AuthenticationResponse>>()
	private val regRepository = AuthorizationRepository()

	/**
	 * Авторизирует пользователя.
	 */
	fun authorize(user: AuthorizationRequest) {
		//Устанавливаем метку как "идущий процесс"
		signinLiveData.value = Resource(Status.PROCESING, null, null)

		//Запускаем процесс
		val job = viewModelScope.launch(Dispatchers.Main + SupervisorJob()){

			//Получаем результат
			val result: Resource<AuthenticationResponse> = try {
				regRepository.makeAuthorizationRequest(user)
			} catch(ex: ConnectTimeoutException) {
				val error: String = getApplication<Application>().getString(R.string.error_http_timeout)
				Resource(Status.CANCELED, null, error)
			} catch(ex: HttpRequestTimeoutException) {
				val error: String = getApplication<Application>().getString(R.string.error_http_timeout)
				Resource(Status.CANCELED, null, error)
			} catch(ex: Exception) {
				val error: String = getApplication<Application>().getString(R.string.error_http_connect)
				Resource(Status.CANCELED, null, error)
			}

			//Устанавливаем значение ресурса ассинхронно
			signinLiveData.postValue(result)
		}
	}
}