package kz.gaudeamus.instudy

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import kz.gaudeamus.instudy.entities.Card
import kz.gaudeamus.instudy.models.CardAdapter
import kz.gaudeamus.instudy.models.CardStudentViewModel
import kz.gaudeamus.instudy.models.Status

class StudentCardContainerFragment : Fragment(), androidx.appcompat.widget.Toolbar.OnMenuItemClickListener {

	/**
	 * Обработчик события на создание карточки из [CreateCardActivity]
	 */
	private val cardAddCallback: ActivityResultLauncher<Card?> = registerForActivityResult(AddCardActivityContract()) {
		it?.let {
			cards.add(it)
			cardAdapter.notifyDataSetChanged()
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

	private var actionMode: ActionMode? = null
	private var pickedCardIndex: Int = 0

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val view = inflater.inflate(R.layout.fragment_student_card_container, container, false)

		//Визуальные компоненты
		val cardList: RecyclerView = view.findViewById(R.id.card_list)
		val appBar: MaterialToolbar = view.findViewById(R.id.main_appbar)

		cardList.adapter = cardAdapter
		appBar.setOnMenuItemClickListener(this)

		//Обработчик нажатия на карточку
		cardAdapter.setOnItemClickListener { card, position ->
			this.pickedCardIndex = position
			//Если не включён режим выбора карточек - запускаем окно изменения карточки
			if(!cardAdapter.multiSelect) cardUpdateCallback.launch(card)
			else actionMode?.finish()
		}

		cardAdapter.setOnLongItemClickListener {
			actionMode = appBar.startActionMode(cardAdapter)
		}

		cardAdapter.setOnCloseActionMode {
			actionMode?.finish()
		}

		cardAdapter.setOnDeleteItemClickListener { list ->
			cardModel.deletedCards.observe(this@StudentCardContainerFragment, { storeData ->
				when(storeData.status) {
					Status.PROCESING -> {

					}
					Status.COMPLETED -> {
						storeData.data?.let { cards.removeAll(it) }
						actionMode?.finish()
					}
					Status.CANCELED -> {

					}
				}
			})

			cardModel.delete(*list.toTypedArray())
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
		})

		cardModel.getAllFromDB()

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
				cardModel.getAllFromDB()
				true
			}
			else -> false
		}
	}

	/**
	 * Класс для передачи данных карточки на страницу создания карточки и получения итового результата от неё.
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