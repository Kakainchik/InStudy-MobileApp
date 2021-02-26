package kz.gaudeamus.instudy

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import kz.gaudeamus.instudy.entities.Account
import kz.gaudeamus.instudy.entities.FilteredCard
import kz.gaudeamus.instudy.models.CardSchoolAdapter
import kz.gaudeamus.instudy.models.CardSchoolViewModel
import kz.gaudeamus.instudy.models.HttpTask.*

class SchoolCardContainerFragment : Fragment(), Toolbar.OnMenuItemClickListener {
	private val cards = mutableListOf<FilteredCard>()
	private val cardModel: CardSchoolViewModel by activityViewModels()
	private val cardAdapter: CardSchoolAdapter = CardSchoolAdapter(cards)

	private lateinit var notificationLayer: LinearLayout
	private lateinit var progressBar: ContentLoadingProgressBar
	private lateinit var cardList: RecyclerView
	private lateinit var currentAccount: Account

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		arguments?.let {
			currentAccount = it[ACCOUNT_ARG] as Account
		}
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val view = inflater.inflate(R.layout.fragment_school_card_container, container, false)

		//Визуальные компоненты
		val appBar: MaterialToolbar = view.findViewById(R.id.main_appbar)
		cardList = view.findViewById(R.id.card_list)
		notificationLayer = view.findViewById(R.id.no_card_image)
		progressBar = view.findViewById(R.id.progressbar)

		cardList.adapter = cardAdapter
		appBar.setOnMenuItemClickListener(this)

		cardAdapter.setOnItemClickListener { card, position ->
			val intent: Intent = Intent(requireContext(), ShowCardActivity::class.java).apply {
				putExtra(ShowCardActivity.NAME_EXTRA, card)
			}
			startActivity(intent)
		}

		//Наблюдаем за получением карточек из локальной базы
		cardModel.localReceivedCards.observe(this, { storeData ->
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
								cardModel.getFromServerByFilterAndSaveInDB(newAccount)
							}
						}
						WebStatus.TIMEOUT, WebStatus.UNABLE_CONNECT -> {
							UIHelper.toastInternetConnectionError(requireContext(), storeData.webStatus)
							/*Если не удаётся сразу подключиться к серверу - просто подгружаем
							имеющиеся из локальной базы*/
							cardModel.getAllFromDB()
							UIHelper.makeEnableUI(true, container!!)
							progressBar.hide()
						}
						else -> {
							UIHelper.makeEnableUI(true, container!!)
							progressBar.hide()
						}
					}
				}
			}
		})

		//Сразу получаем имеющиеся карточки с локальной базы
		cardModel.getFromServerByFilterAndSaveInDB(currentAccount)

		return view
	}

	/**
	 * Обработчик нажатия на верхнее меню.
	 */
	override fun onMenuItemClick(item: MenuItem?): Boolean {
		return when(item?.itemId) {
			R.id.appbar_search_card -> {
				cardModel.getFromServerByFilterAndSaveInDB(currentAccount)
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
		public const val ACCOUNT_ARG = "ACCOUNT"

		@JvmStatic
		public fun newInstance(account: Account): SchoolCardContainerFragment =
			SchoolCardContainerFragment().apply {
				arguments = bundleOf(ACCOUNT_ARG to account)
			}
	}
}