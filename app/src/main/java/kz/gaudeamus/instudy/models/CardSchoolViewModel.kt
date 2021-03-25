package kz.gaudeamus.instudy.models

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kz.gaudeamus.instudy.SingleLiveEvent
import kz.gaudeamus.instudy.database.InStudyDB
import kz.gaudeamus.instudy.database.SchoolCardDAO
import kz.gaudeamus.instudy.entities.*
import kz.gaudeamus.instudy.models.HttpTask.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CardSchoolViewModel : StandardHttpViewModel {
	private val db: InStudyDB
	private val dao: SchoolCardDAO
	protected override val repository = CardRepository()

	public val filterLiveData = MutableLiveData<CardFilter>()
	public val receivedLiveData = SingleLiveEvent<HttpTask<Array<FilteredCardResponse>>>()
	public val localReceivedCards = SingleLiveEvent<List<FilteredCard>>()

	constructor(application: Application) : super(application) {
		//Настраиваем подключение к локальной базе данных
		db = Room.databaseBuilder(application.applicationContext,
								  InStudyDB::class.java,
								  InStudyDB.DATABASE_NAME)
			.enableMultiInstanceInvalidation()
			.fallbackToDestructiveMigration()
			.build()
		dao = db.schoolCardDao()
	}

	override fun onCleared() {
		db.close()
		super.onCleared()
	}

	public fun getFromServerByFilterAndSaveInDB(currentAccount: Account, filter: CardFilter) {
		receivedLiveData.postValue(HttpTask(TaskStatus.PROCESSING, null, WebStatus.NONE))
		viewModelScope.launch(Dispatchers.Main + SupervisorJob()) {
			val result: HttpTask<Array<FilteredCardResponse>> = repository.makeRequest {
				repository.makeGetAllCardByFilterRequest(currentAccount.token, filter)
			}

			if(result.taskStatus == TaskStatus.COMPLETED && result.data != null) {
				db.withTransaction {
					dao.deleteAllCards()
					dao.deleteAllStudents()
				}
				for(r in result.data) {
					val studentInst = Student(email = r.student.email,
											  phone = r.student.phone,
											  name = r.student.name,
											  surname = r.student.surname)

					val id = dao.insertStudent(studentInst)

					val cardInst = Card(title = r.title,
										content = r.content,
										city = r.soughtCity,
										speciality = r.speciality,
										faculty = r.faculty,
										created = LocalDate.parse(r.created, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
										id = r.id,
										status = CardStatus.ACTIVE,
										studentId = id)

					dao.insertCard(cardInst)
				}
			}

			receivedLiveData.postValue(result)
		}
	}

	/**
	 * Получает все карточки с локальной базы данных. Использует [localReceivedCards].
	 */
	public fun getAllFromDB() {
		viewModelScope.launch(Dispatchers.Main + SupervisorJob()) {
			val cards = dao.getAll()
			localReceivedCards.postValue(cards)
		}
	}
}