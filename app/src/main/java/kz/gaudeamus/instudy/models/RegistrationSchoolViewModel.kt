package kz.gaudeamus.instudy.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kz.gaudeamus.instudy.SingleLiveEvent
import kz.gaudeamus.instudy.entities.RegistrationResponse
import kz.gaudeamus.instudy.entities.RegistrationSchoolRequest
import kz.gaudeamus.instudy.models.HttpTask.*

class RegistrationSchoolViewModel(application: Application) : AndroidViewModel(application) {
	public val signinLiveData = SingleLiveEvent<HttpTask<RegistrationResponse>>()
	private val regRepository = AuthorizationRepository()

	/**
	 * Регестрирует школу.
	 */
	public fun registrate(school: RegistrationSchoolRequest) {
		//Устанавливаем метку как "идущий процесс"
		signinLiveData.value = HttpTask(TaskStatus.PROCESSING, null, WebStatus.NONE)

		//Запускаем процесс
		viewModelScope.launch(Dispatchers.Main + SupervisorJob()){
			//Получаем результат
			val result: HttpTask<RegistrationResponse> = regRepository.makeRequest {
				regRepository.makeRegistrationRequest(school)
			}

			//Устанавливаем значение ресурса ассинхронно
			signinLiveData.postValue(result)
		}
	}
}