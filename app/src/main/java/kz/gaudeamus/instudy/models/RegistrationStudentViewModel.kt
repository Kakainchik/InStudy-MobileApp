package kz.gaudeamus.instudy.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kz.gaudeamus.instudy.SingleLiveEvent
import kz.gaudeamus.instudy.entities.RegistrationResponse
import kz.gaudeamus.instudy.entities.RegistrationStudentRequest
import kz.gaudeamus.instudy.models.HttpTask.*

class RegistrationStudentViewModel(application: Application) : StandardHttpViewModel(application) {
	public val signinLiveData = SingleLiveEvent<HttpTask<RegistrationResponse>>()
	protected override val repository = AuthorizationRepository()

	/**
	 * Регестрирует студента.
	 */
	fun register(student: RegistrationStudentRequest) {
		//Устанавливаем метку как "идущий процесс"
		signinLiveData.value = HttpTask(TaskStatus.PROCESSING, null, WebStatus.NONE)

		//Запускаем процесс
		viewModelScope.launch(Dispatchers.Main + SupervisorJob()){

			//Получаем результат
			val result: HttpTask<RegistrationResponse> = repository.makeRequest {
				repository.makeRegistrationRequest(student)
			}

			//Устанавливаем значение ресурса ассинхронно
			signinLiveData.postValue(result)
		}
	}
}