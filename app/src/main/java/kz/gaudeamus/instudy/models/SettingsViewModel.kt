package kz.gaudeamus.instudy.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.launch
import kz.gaudeamus.instudy.SingleLiveEvent
import kz.gaudeamus.instudy.database.CardDAO
import kz.gaudeamus.instudy.database.InStudyDB
import kz.gaudeamus.instudy.entities.Account
import kz.gaudeamus.instudy.entities.UpdatePasswordRequest
import kz.gaudeamus.instudy.models.HttpTask.*

class SettingsViewModel : AndroidViewModel {
	private val db: InStudyDB
	private val repository = AuthorizationRepository()
	public val logoutLiveData = SingleLiveEvent<HttpTask<Nothing>>()
	public val updatePassLiveData = SingleLiveEvent<HttpTask<Nothing>>()

	constructor(application: Application) : super(application) {
		//Настраиваем подключение к локальной базе данных
		db = Room.databaseBuilder(application.applicationContext,
								  InStudyDB::class.java,
								  InStudyDB.DATABASE_NAME)
			.allowMainThreadQueries()
			.fallbackToDestructiveMigration()
			.build()
	}

	/**
	 * Выход из аккаунта и отправка запроса на отзыв Refresh токена.
	 */
	fun logout(currentAccount: Account) {
		logoutLiveData.postValue(HttpTask(TaskStatus.PROCESSING, null, WebStatus.NONE))
		viewModelScope.launch {
			val result: HttpTask<Nothing> = repository.makeRequest {
				repository.makeRevokeTokenRequest(currentAccount.refreshToken)
			}
			logoutLiveData.postValue(result)
			//Очищаем базу данных
			db.clearAllTables()
			db.close()
		}
	}

	/**
	 * Обновляет пароль пользователя.
	 */
	fun updatePassword(currentAccount: Account, request: UpdatePasswordRequest) {
		updatePassLiveData.postValue(HttpTask(TaskStatus.PROCESSING, null, WebStatus.NONE))
		viewModelScope.launch {
			val result: HttpTask<Nothing> = repository.makeRequest {
				repository.makeUpdatePasswordRequest(currentAccount.token, request)
			}
			updatePassLiveData.postValue(result)
		}
	}
}