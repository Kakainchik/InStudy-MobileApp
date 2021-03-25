package kz.gaudeamus.instudy

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import kz.gaudeamus.instudy.entities.Account
import kz.gaudeamus.instudy.entities.CardFilter
import kz.gaudeamus.instudy.entities.FilteredCard
import kz.gaudeamus.instudy.models.CardSchoolAdapter
import kz.gaudeamus.instudy.models.CardSchoolViewModel
import kz.gaudeamus.instudy.models.HttpTask.*

class SchoolCardContainerFragment : Fragment(), Toolbar.OnMenuItemClickListener {
	private val cards = mutableListOf<FilteredCard>()
	private val cardModel: CardSchoolViewModel by activityViewModels()
	private val cardAdapter: CardSchoolAdapter = CardSchoolAdapter(cards)

	//Визуальные компоненты
	private lateinit var notificationLayer: LinearLayout
	private lateinit var progressBar: ContentLoadingProgressBar
	private lateinit var cardList: RecyclerView
	private lateinit var searchText: EditText
	private lateinit var backButton: ImageButton

	private lateinit var currentAccount: Account

	private var filter: CardFilter = CardFilter(null, null, null, null)
	private var filterKind: FilterKind = FilterKind.BY_TITLE

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val view = inflater.inflate(R.layout.fragment_school_card_container, container, false)

		//Инициализируем визуальные компоненты
		val appBar: MaterialToolbar = view.findViewById(R.id.main_appbar)
		cardList = view.findViewById(R.id.card_list)
		notificationLayer = view.findViewById(R.id.no_card_image)
		progressBar = view.findViewById(R.id.progressbar)
		searchText = view.findViewById(R.id.appbar_search_text)
		backButton = view.findViewById(R.id.appbar_back_button)

		currentAccount = IOFileHelper.anyAccountOrNull(requireContext())!!
		cardList.adapter = cardAdapter
		appBar.setOnMenuItemClickListener(this)

		backButton.setOnClickListener {
			searchText.text.clear()
		}

		searchText.addTextChangedListener(object: TextWatcher {
			override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
				//NOTHING
			}

			override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
				//NOTHING
			}

			override fun afterTextChanged(s: Editable?) {
				if(s?.isEmpty()!!) backButton.visibility = View.INVISIBLE
				else backButton.visibility = View.VISIBLE
			}
		})

		//Обработчик события на нажатие по элементу списка
		cardAdapter.setOnItemClickListener { card, position ->
			val intent: Intent = Intent(requireContext(), ShowCardActivity::class.java).apply {
				putExtra(ShowCardActivity.NAME_EXTRA, card)
			}
			startActivity(intent)
		}

		//Наблюдаем за получением карточек из локальной базы
		cardModel.localReceivedCards.observe(this, { storeData ->
			if(storeData.isNotEmpty()) {
				cards.clear()
				cards.addAll(storeData)
				when(filterKind) {
					FilterKind.BY_TITLE -> cards.sortBy { it.card.title }
					FilterKind.BY_CITY -> cards.sortBy { it.card.city }
					FilterKind.BY_DATE -> cards.sortByDescending { it.card.created }
				}
			} else cards.clear()
			cardAdapter.notifyDataSetChanged()
			this.notifyListEmptyOrNot()
		})

		//Наблюдаем за получением карточек из сервера
		cardModel.receivedLiveData.observe(this, { storeData ->
			when(storeData.taskStatus) {
				TaskStatus.PROCESSING -> {
					UIHelper.makeEnableUI(false, container!!)
					progressBar.show()
				}
				TaskStatus.COMPLETED -> {
					cardModel.getAllFromDB()
					UIHelper.makeEnableUI(true, container!!)
					progressBar.hide()
				}
				TaskStatus.CANCELED -> {
					when(storeData.webStatus) {
						//При устаревшем токене - пробуем обновить его и отправить запрос заново
						WebStatus.UNAUTHORIZED -> {
							cardModel.throughRefreshToken(requireContext(), currentAccount) { newAccount ->
								cardModel.getFromServerByFilterAndSaveInDB(newAccount, filter)
							}
						}
						else -> {
							UIHelper.makeEnableUI(true, container!!)
							UIHelper.toastInternetConnectionError(requireContext(), storeData.webStatus)
							progressBar.hide()
						}
					}
				}
			}
		})

		//Наблюдаем за изминениями фильтра из диалогового окна
		cardModel.filterLiveData.observe(viewLifecycleOwner, { storeData ->
			filter = storeData.copy()
			cardModel.getFromServerByFilterAndSaveInDB(currentAccount, filter)
		})

		//Сразу получаем имеющиеся карточки с локальной базы
		cardModel.getAllFromDB()

		return view
	}

	/**
	 * Обработчик нажатия на верхнее меню.
	 */
	override fun onMenuItemClick(item: MenuItem?): Boolean {
		return when(item?.itemId) {
			R.id.appbar_search_card -> {
				cardModel.filterLiveData.postValue(CardFilter(null, null, null, searchText.text.toString()))
				true
			}
			R.id.appbar_filter_card -> {
				val filterDialog = FilterCardDialogFragment(filter)
				filterDialog.show(childFragmentManager, FILTER_DIALOG_TAG)
				true
			}
			R.id.appbar_sort_card -> {
				true
			}
			R.id.appbar_filterby_title -> {
				item.isChecked = !item.isChecked
				filterKind = FilterKind.BY_TITLE
				cards.sortBy { it.card.title }
				cardAdapter.notifyDataSetChanged()
				true
			}
			R.id.appbar_filterby_city -> {
				item.isChecked = !item.isChecked
				filterKind = FilterKind.BY_CITY
				cards.sortBy { it.card.city }
				cardAdapter.notifyDataSetChanged()
				true
			}
			R.id.appbar_filterby_date -> {
				item.isChecked = !item.isChecked
				filterKind = FilterKind.BY_DATE
				cards.sortByDescending { it.card.created }
				cardAdapter.notifyDataSetChanged()
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

	companion object {
		private const val FILTER_DIALOG_TAG = "FILTER_DIALOG"
	}

	/**
	 * Перечислитель для выбора сортировки карточек.
	 */
	private enum class FilterKind {
		BY_TITLE,
		BY_CITY,
		BY_DATE
	}
}