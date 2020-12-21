package kz.gaudeamus.instudy.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kz.gaudeamus.instudy.entities.RegistrationSchool

class RegistrationSchoolViewModel : ViewModel() {
	private val signinLiveData = MutableLiveData<Resource<RegistrationSchool>>()
	private val regRepository = RegistrationRepository()

	public val registrationLiveData : LiveData<Resource<RegistrationSchool>>
			get() = signinLiveData

	//Регестрируем школу
	public fun registrate(school: RegistrationSchool) {
		//Устанавливаем метку как "идущий процесс"
		viewModelScope.launch(Dispatchers.IO) {
			regRepository.makeRegistrationSchoolRequest(school)
		}
	}
}