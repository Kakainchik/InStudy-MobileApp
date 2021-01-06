package kz.gaudeamus.instudy.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.features.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.*
import kz.gaudeamus.instudy.R
import kz.gaudeamus.instudy.SingleLiveEvent
import kz.gaudeamus.instudy.entities.RegistrationResponse
import kz.gaudeamus.instudy.entities.RegistrationSchoolRequest
import java.lang.Exception

class RegistrationSchoolViewModel(application: Application) : AndroidViewModel(application) {
	public val signinLiveData = SingleLiveEvent<Resource<RegistrationResponse>>()
	private val regRepository = AuthorizationRepository()

	/**
	 * Регестрирует школу.
	 */
	public fun registrate(school: RegistrationSchoolRequest) {
		//Устанавливаем метку как "идущий процесс"
		signinLiveData.value = Resource(Status.PROCESING, null, null)

		//Запускаем процесс
		val job = viewModelScope.launch(Dispatchers.Main + SupervisorJob()){

			//Получаем результат
			val result: Resource<RegistrationResponse> = try {
				regRepository.makeRegistrationRequest(school)
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