package kz.gaudeamus.instudy.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import io.ktor.client.features.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kz.gaudeamus.instudy.DATABASE_NAME
import kz.gaudeamus.instudy.IOFileHelper
import kz.gaudeamus.instudy.R
import kz.gaudeamus.instudy.SingleLiveEvent
import kz.gaudeamus.instudy.database.CardDAO
import kz.gaudeamus.instudy.database.InStudyDB
import kz.gaudeamus.instudy.entities.*
import java.lang.Exception

class CardStudentViewModel : AndroidViewModel {
	private val db: InStudyDB
	private val dao: CardDAO
	private val repository: CardRepository = CardRepository()

	public val localAddedCard = SingleLiveEvent<Long?>()
	public val localUpdatedCard = SingleLiveEvent<Boolean>()
	public val localReceivedCards = SingleLiveEvent<List<Card>>()
	public val deletedCards = SingleLiveEvent<Resource<List<Card>>>()
	public val sendLiveData = SingleLiveEvent<Resource<AddCardResponse>>()

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

	fun sendToServer(card: AddCardRequest, currentAccount: Account) {
		sendLiveData.postValue(Resource(Status.PROCESING, null, null))
		viewModelScope.launch(Dispatchers.Main + SupervisorJob()) {
			val result: Resource<AddCardResponse> = try {
				repository.makeAddCardRequest(card, currentAccount.token)
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

			sendLiveData.postValue(result)
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

	fun delete(vararg cards: Card) {
		viewModelScope.launch {
			val list = mutableListOf<Card>()
			cards.forEach {
				deletedCards.postValue(Resource(Status.PROCESING, null, null))
				try {
					if(it.status == CardStatus.DRAFT) {
						dao.delete(it)
						list.add(it)
					} else {

					}
				} catch(ex: Exception) {
					deletedCards.postValue(Resource(Status.CANCELED, null, ex.message))
				}
			}

			deletedCards.postValue(Resource(Status.COMPLETED, list, null))
		}
	}
}