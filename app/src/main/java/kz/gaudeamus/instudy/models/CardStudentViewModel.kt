package kz.gaudeamus.instudy.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kz.gaudeamus.instudy.SingleLiveEvent
import kz.gaudeamus.instudy.database.CardDAO
import kz.gaudeamus.instudy.database.InStudyDB
import kz.gaudeamus.instudy.database.InStudyDB.Companion.DATABASE_NAME
import kz.gaudeamus.instudy.entities.*
import kz.gaudeamus.instudy.models.HttpTask.*
import java.lang.Exception
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CardStudentViewModel : StandardHttpViewModel {
	private val db: InStudyDB
	private val dao: CardDAO
	protected override val repository = CardRepository()

	public val localAddedCard = SingleLiveEvent<Long?>()
	public val localUpdatedCard = SingleLiveEvent<Boolean>()
	public val localReceivedCards = SingleLiveEvent<List<Card>>()
	public val deletedCards = SingleLiveEvent<HttpTask<List<Card>>>()
	public val sendLiveData = SingleLiveEvent<HttpTask<CardResponse>>()
	public val receivedLiveData = SingleLiveEvent<HttpTask<Array<CardResponse>>>()

	constructor(application: Application) : super(application) {
		//Настраиваем подключение к локальной базе данных
		db = Room.databaseBuilder(application.applicationContext,
									  InStudyDB::class.java,
									  DATABASE_NAME)
			.enableMultiInstanceInvalidation()
			.fallbackToDestructiveMigration()
			.build()
		dao = db.cardDao()
	}

	override fun onCleared() {
		db.close()
		super.onCleared()
	}

	fun sendToServer(card: AddCardRequest, currentAccount: Account) {
		sendLiveData.postValue(HttpTask(TaskStatus.PROCESSING, null, WebStatus.NONE))
		viewModelScope.launch(Dispatchers.Main + SupervisorJob()) {
			val result: HttpTask<CardResponse> = repository.makeRequest {
				repository.makeAddCardRequest(card, currentAccount.token)
			}

			sendLiveData.postValue(result)
		}
	}

	/**
	 * Получает все свои карточки, хранящиеся на сервере. Использует [receivedLiveData]
	 */
	fun getOwnFromServerAndMergeWithDB(currentAccount: Account) {
		receivedLiveData.postValue(HttpTask(TaskStatus.PROCESSING, null, WebStatus.NONE))
		viewModelScope.launch(Dispatchers.Main + SupervisorJob()) {
			val result: HttpTask<Array<CardResponse>> = repository.makeRequest {
				repository.makeGetOwnCardRequest(currentAccount.token)
			}

			if(result.taskStatus == TaskStatus.COMPLETED && result.data != null) {
				val onlineCards: Array<Card> = Array(result.data.size) {
					with(result.data[it]) {
						Card(title = this.title,
							 content = this.content,
							 city = this.soughtCity,
							 speciality = this.speciality,
							 faculty = this.faculty,
							 created = LocalDate.parse(this.created, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
							 id = this.id,
							 status = if(this.isValid) CardStatus.ACTIVE else CardStatus.EXPIRED)
					}
				}

				//Прошлые активные карточки считаем за старые и перезаписываем.
				dao.deleteAllNonDrafts()
				dao.insertAll(*onlineCards)
			}

			receivedLiveData.postValue(result)
		}
	}

	/**
	 * Добавляет карточку в локальную базу данных. Использует [localAddedCard].
	 */
	fun addToDB(card: Card) {
		viewModelScope.launch(Dispatchers.Main + SupervisorJob()) {
			try {
				val id = dao.insertAll(card)
				localAddedCard.postValue(id[0])
			} catch(ex: Exception) {
				localAddedCard.postValue(null)
			}
		}
	}

	/**
	 * Получает все карточки с локальной базы данных. Использует [localReceivedCards].
	 */
	fun getAllFromDB() {
		viewModelScope.launch(Dispatchers.Main + SupervisorJob()) {
			val cards = dao.getAll()
			localReceivedCards.postValue(cards)
		}
	}

	/**
	 * Обновляет определённую карточку локально. Использует [localUpdatedCard].
	 */
	fun updateToDB(card: Card): Unit {
		viewModelScope.launch {
			try {
				dao.update(card)
				localUpdatedCard.postValue(true)
			} catch(ex: Exception) {
				localUpdatedCard.postValue(false)
			}
		}
	}

	/**
	 * Удаляет определённые карточки и локально, и с сервера.
	 */
	fun delete(currentAccount: Account, vararg cards: Card) {
		viewModelScope.launch {
			val list = mutableListOf<Card>()
			deletedCards.postValue(HttpTask(TaskStatus.PROCESSING, null, WebStatus.NONE))
			cards.forEach {
				try {
					//Если это черновик - удаляем локально и просто
					if(it.status == CardStatus.DRAFT) {
						dao.delete(it)
						list.add(it)
					//Если активная - отправляем запрос на сервер
					} else {
						val task = repository.makeRequest {
							repository.makeDeleteCardRequest(currentAccount.token, it.id!!)
						}
						if(task.data != null && task.data) {
							dao.delete(it)
							list.add(it)
						} else {
							deletedCards.postValue(HttpTask(TaskStatus.PROCESSING, null, task.webStatus))
							return@forEach
						}
					}
				} catch(ex: Exception) {
					deletedCards.postValue(HttpTask(TaskStatus.CANCELED, null, WebStatus.NONE))
				}
			}

			deletedCards.postValue(HttpTask(TaskStatus.COMPLETED, list, WebStatus.NONE))
		}
	}
}