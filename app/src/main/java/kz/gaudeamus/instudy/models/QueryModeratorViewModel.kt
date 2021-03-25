package kz.gaudeamus.instudy.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kz.gaudeamus.instudy.SingleLiveEvent
import kz.gaudeamus.instudy.database.InStudyDB
import kz.gaudeamus.instudy.database.QueryDAO
import kz.gaudeamus.instudy.entities.Account
import kz.gaudeamus.instudy.entities.PropsResponse
import kz.gaudeamus.instudy.entities.SchoolQuery
import kz.gaudeamus.instudy.models.HttpTask.*

class QueryModeratorViewModel : StandardHttpViewModel {
	private val db: InStudyDB
	private val dao: QueryDAO
	protected override val repository: QueryRepository = QueryRepository()

	public val receivedLiveData = SingleLiveEvent<HttpTask<Array<SchoolQuery>>>()
	public val propsLiveData = SingleLiveEvent<HttpTask<Array<PropsResponse>>>()
	public val verifyLiveData = SingleLiveEvent<HttpTask<Boolean>>()
	public val denyLiveData = SingleLiveEvent<HttpTask<Boolean>>()

	constructor(application: Application) : super(application) {
		db = Room.databaseBuilder(application.applicationContext,
								  InStudyDB::class.java,
								  InStudyDB.DATABASE_NAME)
			.enableMultiInstanceInvalidation()
			.fallbackToDestructiveMigration()
			.build()
		dao = db.queryDao()
	}

	fun getAllFromServerAndMergeWithDB(currentAccount: Account) {
		receivedLiveData.postValue(HttpTask(TaskStatus.PROCESSING, null, WebStatus.NONE))
		viewModelScope.launch(Dispatchers.Main + SupervisorJob()) {
			val result: HttpTask<Array<SchoolQuery>> = repository.makeRequest {
				repository.makeGetUnverifiedSchoolsRequest(currentAccount.token)
			}

			//Если получили данные, то записываем в базу
			result.data.takeIf { result.taskStatus == TaskStatus.COMPLETED }?.let {
				dao.deleteAll()
				dao.insertAll(*it)
			}

			receivedLiveData.postValue(result)
		}
	}

	fun getPropsBySchoolId(currentAccount: Account, id: Long) {
		propsLiveData.postValue(HttpTask(TaskStatus.PROCESSING, null, WebStatus.NONE))
		viewModelScope.launch(Dispatchers.Main + SupervisorJob()) {
			val result: HttpTask<Array<PropsResponse>> = repository.makeRequest {
				repository.makeGetPropsRequest(currentAccount.token, id)
			}

			propsLiveData.postValue(result)
		}
	}

	fun verifyQuery(currentAccount: Account, id: Long) {
		verifyLiveData.postValue(HttpTask(TaskStatus.PROCESSING, null, WebStatus.NONE))
		viewModelScope.launch(Dispatchers.Main + SupervisorJob()) {
			val result: HttpTask<Boolean> = repository.makeRequest {
				repository.makeVerifyQueryRequest(currentAccount.token, id)
			}

			verifyLiveData.postValue(result)
		}
	}

	fun denyQuery(currentAccount: Account, id: Long, comment: String? = null) {
		denyLiveData.postValue(HttpTask(TaskStatus.PROCESSING, null, WebStatus.NONE))
		viewModelScope.launch(Dispatchers.Main + SupervisorJob()) {
			val result: HttpTask<Boolean> = repository.makeRequest {
				repository.makeDenyQueryRequest(currentAccount.token, id, comment)
			}

			denyLiveData.postValue(result)
		}
	}

	override fun onCleared() {
		db.close()
		super.onCleared()
	}
}