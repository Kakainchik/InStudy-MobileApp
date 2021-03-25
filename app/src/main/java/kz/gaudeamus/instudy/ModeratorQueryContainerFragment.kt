package kz.gaudeamus.instudy

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import kz.gaudeamus.instudy.entities.Account
import kz.gaudeamus.instudy.entities.SchoolQuery
import kz.gaudeamus.instudy.models.HttpTask
import kz.gaudeamus.instudy.models.HttpTask.*
import kz.gaudeamus.instudy.models.QueryModeratorViewModel
import kz.gaudeamus.instudy.models.QueryAdapter

class ModeratorQueryContainerFragment : Fragment(), Toolbar.OnMenuItemClickListener {

	private val queries = mutableListOf<SchoolQuery>()
	private val queryAdapter: QueryAdapter = QueryAdapter(queries)
	private val queryModel: QueryModeratorViewModel by activityViewModels()

	private lateinit var notificationLayer: LinearLayout
	private lateinit var progressBar: ContentLoadingProgressBar
	private lateinit var cardList: RecyclerView
	private lateinit var currentAccount: Account

	private var pickedQueryIndex: Int = 0
	private var isFirstLoad: Boolean = true

	/**
	 * Обработчик события на подтверждение регистрации школы из [VerifyingQueryActivity]
	 */
	private val verifyQueryCallback: ActivityResultLauncher<SchoolQuery> = registerForActivityResult(VerifyQueryActivityContract()) {
		it?.let {
			queries.removeAt(pickedQueryIndex)
			queryAdapter.notifyItemRemoved(pickedQueryIndex)
		}
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val view = inflater.inflate(R.layout.fragment_moderator_query_container, container, false)

		//Визуальные компоненты
		val appBar: MaterialToolbar = view.findViewById(R.id.main_appbar)
		cardList = view.findViewById(R.id.card_list)
		notificationLayer = view.findViewById(R.id.no_query_image)
		progressBar = view.findViewById(R.id.progressbar)

		currentAccount = IOFileHelper.anyAccountOrNull(requireContext())!!
		cardList.adapter = queryAdapter
		appBar.setOnMenuItemClickListener(this)

		//Обработчик нажатия на элемент списка
		queryAdapter.setOnItemClickListener { query, index ->
			pickedQueryIndex = index
			verifyQueryCallback.launch(query)
		}

		//Наблюдаем за получение запросов с сервера
		queryModel.receivedLiveData.observe(this, { storeData ->
			when(storeData.taskStatus) {
				TaskStatus.PROCESSING -> {
					//Если это первый(автоматический) запуск - не блокируем UI
					if(isFirstLoad) {
						isFirstLoad = false
						return@observe
					}
					UIHelper.makeEnableUI(false, container!!)
					progressBar.show()
				}
				TaskStatus.COMPLETED -> {
					storeData.data?.let {
						queries.clear()
						queries.addAll(it)
						queryAdapter.notifyDataSetChanged()
					}
					UIHelper.makeEnableUI(true, container!!)
					progressBar.hide()
				}
				TaskStatus.CANCELED -> {
					//При устаревшем токене - пробуем обновить его и отправить запрос заново
					if(storeData.webStatus == WebStatus.UNAUTHORIZED) {
						queryModel.throughRefreshToken(requireContext(), currentAccount) { newAccount ->
							queryModel.getAllFromServerAndMergeWithDB(newAccount)
						}
					} else {
						UIHelper.makeEnableUI(true, container!!)
						progressBar.hide()
					}
				}
			}
		})

		//Сразу получаем список запросов
		queryModel.getAllFromServerAndMergeWithDB(currentAccount)

		return view
	}

	override fun onMenuItemClick(item: MenuItem?): Boolean {
		return when(item?.itemId) {
			//Получаем все имеющиеся запросы
			R.id.appbar_refresh_query -> {
				queryModel.getAllFromServerAndMergeWithDB(currentAccount)
				true
			}
			else -> false
		}
	}

	private class VerifyQueryActivityContract : ActivityResultContract<SchoolQuery, SchoolQuery?>() {
		override fun createIntent(context: Context, input: SchoolQuery?): Intent {
			return Intent(context, VerifyingQueryActivity::class.java).apply {
				this.putExtra(VerifyingQueryActivity.NAME_EXTRA, input)
			}
		}

		override fun parseResult(resultCode: Int, intent: Intent?): SchoolQuery? {
			val data = intent?.getSerializableExtra(VerifyingQueryActivity.NAME_EXTRA) as? SchoolQuery
			return data.takeIf { resultCode == Activity.RESULT_OK && it != null }
		}
	}
}