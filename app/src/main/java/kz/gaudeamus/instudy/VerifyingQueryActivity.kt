package kz.gaudeamus.instudy

import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.core.widget.ContentLoadingProgressBar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.skydoves.expandablelayout.ExpandableLayout
import kz.gaudeamus.instudy.entities.PropsResponse
import kz.gaudeamus.instudy.entities.SchoolQuery
import kz.gaudeamus.instudy.models.HttpTask.*
import kz.gaudeamus.instudy.models.QueryModeratorViewModel
import org.xmlpull.v1.XmlPullParser

class VerifyingQueryActivity : AppCompatActivity(), DenyQueryDialogFragment.DeniableListener {
	//Визуальные компоненты
	private lateinit var emailText: MaterialTextView
	private lateinit var organizationText: MaterialTextView
	private lateinit var createdText: MaterialTextView
	private lateinit var expandableLayout: ExpandableLayout
	private lateinit var denyButton: MaterialButton
	private lateinit var verifyButton: MaterialButton
	private lateinit var propsList: ListView
	private lateinit var progressBar: ContentLoadingProgressBar
	private lateinit var container: ConstraintLayout
	private lateinit var propsAdapter: ArrayAdapter<String>
	private lateinit var bundle: SchoolQuery

	private val props = mutableListOf<String>()
	private val queryModel: QueryModeratorViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_verifying_query)

		//Инициализируем визуальные компоненты
		emailText = findViewById(R.id.verifying_query_email_title)
		organizationText = findViewById(R.id.verifying_query_organization_title)
		createdText = findViewById(R.id.verifying_query_created_title)
		expandableLayout = findViewById(R.id.verifying_query_expandable)
		denyButton = findViewById(R.id.verifying_query_deny_button)
		verifyButton = findViewById(R.id.verifying_query_verify_button)
		propsList = expandableLayout.secondLayout.findViewById(R.id.props_list)
		progressBar = findViewById(R.id.progressbar)
		container = findViewById(R.id.verifying_query_container)
		propsAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, props)
		val propsBar = expandableLayout.parentLayout.findViewWithTag<ImageView>("props_progress_bar")

		//В любом случае на эту страницу должна прийти сущность запроса
		bundle = intent.getSerializableExtra(NAME_EXTRA) as SchoolQuery
		propsList.adapter = propsAdapter

		//Показываем данные
		emailText.text = getString(R.string.title_query_email, bundle.email)
		organizationText.text = getString(R.string.title_query_organization, bundle.organization)
		createdText.text = getString(R.string.title_query_created, bundle.created)

		val currentAccount = IOFileHelper.anyAccountOrNull(this)!!

		//Обработчик нажатия на элемент списка
		propsList.setOnItemClickListener { _, _, position, _ ->
			val intent = Intent().apply {
				addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
				addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
				addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
				setAction(Intent.ACTION_VIEW)
				val file = IOFileHelper.takePropsFile(this@VerifyingQueryActivity,
													  bundle.id, props[position])
				val apkUri = FileProvider.getUriForFile(this@VerifyingQueryActivity,
														this@VerifyingQueryActivity.applicationContext.packageName + ".provider",
														file)
				val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
				setDataAndType(apkUri, mime)
			}
			startActivity(Intent.createChooser(intent, getText(R.string.intent_open_with)))
		}

		//Наблбюдаем за получением реквизитов с сервера при нажатии на открытие выпадающего списка
		expandableLayout.parentLayout.setOnClickListener {
			if(expandableLayout.isExpanded) return@setOnClickListener
			queryModel.propsLiveData.observe(this, { storeData ->
				when(storeData.taskStatus) {
					TaskStatus.PROCESSING -> {
						//Анимация загрузки
						propsBar.visibility = View.VISIBLE
					}
					TaskStatus.COMPLETED -> {
						bundle.props = storeData.data!!.map<PropsResponse, String> { it.name }
						storeData.data.forEach {
							this.props.add(it.name)
							IOFileHelper.saveProps(this, bundle.id, it.name, Base64.decode(it.data, Base64.NO_WRAP))
						}
						propsAdapter.notifyDataSetChanged()
						expandableLayout.expand()
						propsBar.visibility = View.INVISIBLE
					}
					TaskStatus.CANCELED -> {
						//При устаревшем токене - пробуем обновить его и отправить запрос заново
						if(storeData.webStatus == WebStatus.UNAUTHORIZED) {
							queryModel.throughRefreshToken(this, currentAccount) { newAccount ->
								queryModel.getPropsBySchoolId(newAccount, bundle.id)
							}
						} else propsBar.visibility = View.INVISIBLE
					}
				}
			})

			queryModel.getPropsBySchoolId(currentAccount, bundle.id)
		}

		//Наблюдаем за подтверждением регистрации
		queryModel.verifyLiveData.observe(this, { storeData ->
			when(storeData.taskStatus) {
				TaskStatus.PROCESSING -> {
					progressBar.show()
					UIHelper.makeEnableUI(false, container)
				}
				TaskStatus.COMPLETED -> {
					Toast.makeText(this, getText(R.string.notice_query_verified), Toast.LENGTH_SHORT).show()
					setResult(RESULT_OK, Intent().putExtra(NAME_EXTRA, bundle))
					finish()
				}
				TaskStatus.CANCELED -> {
					//При устаревшем токене - пробуем обновить его и отправить запрос заново
					if(storeData.webStatus == WebStatus.UNAUTHORIZED) {
						queryModel.throughRefreshToken(this, currentAccount) { newAccount ->
							queryModel.verifyQuery(newAccount, bundle.id)
						}
					} else {
						if(storeData.data == false)
							Toast.makeText(this, R.string.error_cannot_verify_query, Toast.LENGTH_SHORT).show()
						progressBar.hide()
						UIHelper.makeEnableUI(true, container)
					}
				}
			}
		})

		//Обработчик нажатия на кнопку отклонения регистрации
		denyButton.setOnClickListener {
			val denyDialog = DenyQueryDialogFragment()
			denyDialog.show(supportFragmentManager, DENY_DIALOG_TAG)
		}

		//Обработчик нажатия на кнопку подтверждения регистрации
		verifyButton.setOnClickListener {
			val alertDialog: AlertDialog? = MaterialAlertDialogBuilder(this).apply {
				setTitle(R.string.title_attention)
				setMessage(R.string.alert_sure_verify_query_text)
				setPositiveButton(R.string.bt_ok) { dialog: DialogInterface, id: Int ->
					queryModel.verifyQuery(currentAccount, bundle.id)
					dialog.dismiss()
				}
				setNegativeButton(R.string.bt_cancel) { dialog, id ->
					dialog.cancel()
				}
			}.create()

			//Показываем уведомление, чтобы избежать подтверждения при случайном нажатии
			alertDialog?.show()
		}
	}

	override fun onDeny(comment: String?) {
		val currentAccount = IOFileHelper.anyAccountOrNull(this)!!

		//Наблюдаем за отменой регистрации
		queryModel.denyLiveData.observe(this, { storeData ->
			when(storeData.taskStatus) {
				TaskStatus.PROCESSING -> {
					progressBar.show()
					UIHelper.makeEnableUI(false, container)
				}
				TaskStatus.COMPLETED -> {
					Toast.makeText(this, getText(R.string.notice_query_denied), Toast.LENGTH_SHORT).show()
					Log.d(DENY_DIALOG_TAG, "Start denying a query")
					setResult(RESULT_OK, Intent().putExtra(NAME_EXTRA, bundle))
					finish()
				}
				TaskStatus.CANCELED -> {
					//При устаревшем токене - пробуем обновить его и отправить запрос заново
					if(storeData.webStatus == WebStatus.UNAUTHORIZED) {
						queryModel.throughRefreshToken(this, currentAccount) { newAccount ->
							queryModel.denyQuery(newAccount, bundle.id, comment)
						}
					} else {
						if(storeData.data == false)
							Toast.makeText(this, R.string.error_cannot_deny_query, Toast.LENGTH_SHORT).show()
						progressBar.hide()
						UIHelper.makeEnableUI(true, container)
					}
				}
			}
		})

		//Отменяем регистрацию
		queryModel.denyQuery(currentAccount, bundle.id, comment)
	}

	companion object {
		internal const val NAME_EXTRA = "QUERY"
		private const val DENY_DIALOG_TAG = "DENY_DIALOG"
	}
}