package kz.gaudeamus.instudy.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kz.gaudeamus.instudy.SingleLiveEvent
import kz.gaudeamus.instudy.entities.*
import kz.gaudeamus.instudy.models.HttpTask.*

class AuthorizationViewModel(application: Application) : StandardHttpViewModel(application) {
	protected override val repository = AuthorizationRepository()
	public val signinLiveData = SingleLiveEvent<HttpTask<AuthenticationResponse>>()
	public val receivedPersonalInformation = SingleLiveEvent<InformationResponse?>()

	/**
	 * Авторизирует пользователя.
	 */
	fun authorize(user: AuthorizationRequest) {
		//Устанавливаем метку как "идущий процесс"
		signinLiveData.value = HttpTask(TaskStatus.PROCESSING, null, WebStatus.NONE)

		//Запускаем процесс
		viewModelScope.launch(Dispatchers.Main + SupervisorJob()){

			//Получаем результат
			val result: HttpTask<AuthenticationResponse> = repository.makeRequest {
				repository.makeAuthorizationRequest(user)
			}

			//Устанавливаем значение ресурса ассинхронно
			signinLiveData.postValue(result)
		}
	}

	/**
	 * Получает персональные данные.
	 */
	fun receivePersonalInformation(user: Account) {
		//Устанавливаем метку как "идущий процесс"
		receivedPersonalInformation.value = null

		//Запускаем процесс
		viewModelScope.launch(Dispatchers.Main + SupervisorJob()){

			//Получаем результат
			val result = repository.makeGetPersonalInformationRequest(user.token, user.kind)

			//Устанавливаем значение ресурса ассинхронно
			receivedPersonalInformation.postValue(result)
		}
	}
}