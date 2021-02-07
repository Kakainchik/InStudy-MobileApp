package kz.gaudeamus.instudy.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kz.gaudeamus.instudy.SingleLiveEvent
import kz.gaudeamus.instudy.entities.AuthenticationResponse
import kz.gaudeamus.instudy.entities.AuthorizationRequest
import kz.gaudeamus.instudy.models.HttpTask.*

class AuthorizationViewModel(application: Application) : AndroidViewModel(application) {
	public val signinLiveData = SingleLiveEvent<HttpTask<AuthenticationResponse>>()
	private val regRepository = AuthorizationRepository()

	/**
	 * Авторизирует пользователя.
	 */
	fun authorize(user: AuthorizationRequest) {
		//Устанавливаем метку как "идущий процесс"
		signinLiveData.value = HttpTask(TaskStatus.PROCESSING, null, WebStatus.NONE)

		//Запускаем процесс
		viewModelScope.launch(Dispatchers.Main + SupervisorJob()){

			//Получаем результат
			val result: HttpTask<AuthenticationResponse> = regRepository.makeRequest {
				regRepository.makeAuthorizationRequest(user)
			}

			//Устанавливаем значение ресурса ассинхронно
			signinLiveData.postValue(result)
		}
	}
}