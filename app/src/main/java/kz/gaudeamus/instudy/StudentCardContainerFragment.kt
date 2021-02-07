package kz.gaudeamus.instudy

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import kz.gaudeamus.instudy.entities.Card
import kz.gaudeamus.instudy.models.CardAdapter
import kz.gaudeamus.instudy.models.CardStudentViewModel
import kz.gaudeamus.instudy.models.HttpTask.*

class StudentCardContainerFragment : Fragment(R.layout.fragment_student_card_container), androidx.appcompat.widget.Toolbar.OnMenuItemClickListener {
	/**
	 * Обработчик события на создание карточки из [CreateCardActivity]
	 */
	private val cardAddCallback: ActivityResultLauncher<Card?> = registerForActivityResult(AddCardActivityContract()) {
		it?.let {
			cards.add(it)
			cardAdapter.notifyDataSetChanged()
			this.notifyListEmptyOrNot()
		}
	}

	/**
	 * Обработчик события на обновление карточки из [CreateCardActivity]
	 */
	private val cardUpdateCallback: ActivityResultLauncher<Card?> = registerForActivityResult(AddCardActivityContract()) {
		it?.let { new ->
			val index = cards.indexOfFirst { old -> old.guid == new.guid }
			cards[index] = new
			cardAdapter.notifyItemChanged(pickedCardIndex)
		}
	}

	private val cards = mutableListOf<Card>()
	private val cardModel: CardStudentViewModel by activityViewModels()
	private val cardAdapter: CardAdapter = CardAdapter(cards)

	private lateinit var notificationLayer: LinearLayout
	private lateinit var progressBar: ContentLoadingProgressBar
	private lateinit var cardList: RecyclerView

	private var actionMode: ActionMode? = null
	private var pickedCardIndex: Int = 0

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val view = inflater.inflate(R.layout.fragment_student_card_container, container, false)

		//Визуальные компоненты
		val appBar: MaterialToolbar = view.findViewById(R.id.main_appbar)
		cardList = view.findViewById(R.id.card_list)
		notificationLayer = view.findViewById(R.id.no_card_image)
		progressBar = view.findViewById(R.id.progressbar)

		cardList.adapter = cardAdapter
		appBar.setOnMenuItemClickListener(this)

		//Обработчик нажатия на карточку
		cardAdapter.setOnItemClickListener { card, position ->
			this.pickedCardIndex = position
			//Если не включён режим выбора карточек - запускаем окно изменения карточки
			if(!cardAdapter.multiSelect) cardUpdateCallback.launch(card)
			else actionMode?.finish()
		}

		//Обработчик долгого нажатия на карточку
		cardAdapter.setOnLongItemClickListener {
			actionMode = appBar.startActionMode(cardAdapter)
		}

		//Если получаем оповещение о завершении работы контекстуального меню - закрываем его
		cardAdapter.setOnCloseActionMode {
			actionMode?.finish()
		}

		//Обработчик нажатия на кнопку удаления карточки в контестном меню
		cardAdapter.setOnDeleteItemClickListener { list ->
			//Наблюдаем за работой удаления карточек
			cardModel.deletedCards.observe(this@StudentCardContainerFragment, { storeData ->
				when(storeData.taskStatus) {
					TaskStatus.PROCESSING -> {

					}
					TaskStatus.COMPLETED -> {
						storeData.data?.let { cards.removeAll(it) }
						actionMode?.finish()
						cardAdapter.notifyDataSetChanged()
						this.notifyListEmptyOrNot()
					}
					TaskStatus.CANCELED -> {

					}
				}
			})

			val account = IOFileHelper.anyAccountOrNull(requireContext())
			account?.let { cardModel.delete(it, *list.toTypedArray()) }
		}


		//Наблюдаем за получением карточек из локальной базы
		cardModel.localReceivedCards.observe(this.viewLifecycleOwner, { storeData ->
			if(storeData.isNotEmpty()) {
				cards.apply {
					this.clear()
					this.addAll(storeData)
				}
				cardAdapter.notifyDataSetChanged()
			}
			this.notifyListEmptyOrNot()
		})

		//Наблюдаем за получением карточек из сервера
		cardModel.receivedLiveData.observe(this.viewLifecycleOwner, { storeData ->
			when(storeData.taskStatus) {
				TaskStatus.PROCESSING -> {
					UIHelper.makeEnableUI(false, container!!)
					progressBar.visibility = View.VISIBLE
				}
				TaskStatus.COMPLETED -> {
					cardModel.getAllFromDB()
					UIHelper.makeEnableUI(true, container!!)
					progressBar.visibility = View.GONE
				}
				TaskStatus.CANCELED -> {
					UIHelper.makeEnableUI(true, container!!)
					progressBar.visibility = View.GONE
				}
			}
		})

		//Запускаем работу на получение карточек с локальной базы
		cardModel.getAllFromDB()

		//FIXME Метод запускается каждый раз при транзакции фрагментов, отчего картинка на фоне мерцает
		return view
	}

	/**
	 * Обработчик нажатия на верхнее меню.
	 */
	override fun onMenuItemClick(item: MenuItem?): Boolean {
		return when(item?.itemId) {
			//Создаём новую карточку
			R.id.appbar_add_card -> {
				cardAddCallback.launch(null)
				true
			}
			//Получаем все имеющиеся локальные карточки
			R.id.appbar_refresh_card -> {
				val account = IOFileHelper.anyAccountOrNull(requireContext())
				account?.let { cardModel.getOwnFromServerAndMergeWithDB(it) }
				true
			}
			else -> false
		}
	}

	/**
	 * Устанавливает оповещение о пустом списке карточек, если это так.
	 */
	private fun notifyListEmptyOrNot() {
		if(cards.isEmpty()) {
			notificationLayer.visibility = View.VISIBLE
			cardList.visibility = View.GONE
		} else {
			notificationLayer.visibility = View.GONE
			cardList.visibility = View.VISIBLE
		}
	}

	/**
	 * Класс для передачи данных карточки на страницу создания карточки и получения итогового результата от неё.
	 */
	private class AddCardActivityContract : ActivityResultContract<Card?, Card?>() {
		override fun createIntent(context: Context, input: Card?): Intent {
			return Intent(context, CreateCardActivity::class.java).apply {
				this.putExtra(CreateCardActivity.NAME, input)
			}
		}

		override fun parseResult(resultCode: Int, intent: Intent?): Card? {
			val data = intent?.getSerializableExtra(CreateCardActivity.NAME) as? Card
			return data.takeIf { resultCode == CreateCardActivity.ACTIVITY_CODE && it != null }
		}
	}
}