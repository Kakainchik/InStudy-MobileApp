package kz.gaudeamus.instudy

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.annotation.ColorRes
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.ContentLoadingProgressBar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kz.gaudeamus.instudy.entities.AddCardRequest
import kz.gaudeamus.instudy.entities.Card
import kz.gaudeamus.instudy.entities.CardStatus
import kz.gaudeamus.instudy.entities.UpdateCardRequest
import kz.gaudeamus.instudy.models.CardStudentViewModel
import kz.gaudeamus.instudy.models.HttpTask.*
import java.time.LocalDate

class CreateCardActivity : AppCompatActivity() {

	private val cardModel: CardStudentViewModel by viewModels()

	//Визуальные компоненты
	private lateinit var titleText: EditText
	private lateinit var contentText: EditText
	private lateinit var draftButton: MaterialButton
	private lateinit var createButton: MaterialButton
	private lateinit var cityAutoText: AutoCompleteTextView
	private lateinit var facultiesAutoText: AutoCompleteTextView
	private lateinit var specialitiesAutoText: AutoCompleteTextView
	private lateinit var progressBar: ContentLoadingProgressBar
	private lateinit var container: ScrollView
	private lateinit var settings: SharedPreferences

	private var currentCard: Card? = null
	private var bundle: Card? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_create_card)

		//Инициализируем визуальные компоненты
		titleText = findViewById(R.id.create_card_title_text)
		contentText = findViewById(R.id.create_card_content_text)
		draftButton = findViewById(R.id.create_card_save2draft_button)
		createButton = findViewById(R.id.create_card_create_button)
		cityAutoText = findViewById(R.id.cities_autotext)
		facultiesAutoText = findViewById(R.id.faculties_autotext)
		specialitiesAutoText = findViewById(R.id.specialities_autotext)
		progressBar = findViewById(R.id.progressbar)
		container = findViewById(R.id.create_card_container)

		settings = getSharedPreferences("PERSONAL_DATA_KEY", Context.MODE_PRIVATE)
		val account = IOFileHelper.anyAccountOrNull(this)!!
		bundle = intent.getSerializableExtra(NAME_EXTRA) as? Card
		//Заполняем поля, если редактируем карточку, а не создаём новую
		bundle?.apply {
			titleText.setText(title)
			contentText.setText(content)
			draftButton.text = getString(R.string.bt_keep_draft)
			cityAutoText.setText(city)
			facultiesAutoText.setText(faculty)
			specialitiesAutoText.setText(speciality)

			if(status == CardStatus.DRAFT) createButton.text = getString(R.string.bt_publish)
			else createButton.text = getString(R.string.bt_save)

			if(status != CardStatus.DRAFT) draftButton.visibility = View.GONE
			if(status == CardStatus.EXPIRED) {
				createButton.visibility = View.GONE
				titleText.isEnabled = false
				contentText.isEnabled = false
				cityAutoText.isEnabled = false
				facultiesAutoText.isEnabled = false
				specialitiesAutoText.isEnabled = false
			}
		}

		cityAutoText.apply {
			this.setAdapter(ArrayAdapter.createFromResource(this@CreateCardActivity,
															R.array.cities,
															android.R.layout.simple_spinner_item))
			if(bundle == null) {
				this.adapter.getItem(0) as String
				if(settings.contains("DEFAULT_CITY")) this.setText(settings.getString("DEFAULT_CITY", null))
			}
		}

		//Наблюдаем за добавлением карточки на сервер
		cardModel.sendLiveData.observe(this, { storeData ->
			when(storeData.taskStatus) {
				TaskStatus.PROCESSING -> {
					progressBar.show()
					UIHelper.makeEnableUI(false, container)
				}
				//Если отправка на сервер прошла успешно
				TaskStatus.COMPLETED -> {
					progressBar.hide()
					UIHelper.makeEnableUI(true, container)

					//Обновляем id полученной карты и создаём её локально
					storeData.data?.apply {
						currentCard!!.id = this.id
						currentCard!!.status = if(this.isValid) CardStatus.ACTIVE else CardStatus.EXPIRED
						cardModel.addToDB(currentCard!!)
					}
				}
				TaskStatus.CANCELED -> {
					//При устаревшем токене - пробуем обновить его и отправить запрос заново
					if(storeData.webStatus == WebStatus.UNAUTHORIZED) {
						cardModel.throughRefreshToken(this, account) { newAccount ->
							cardModel.sendToServer(currentCard!!, newAccount)
						}
					} else {
						progressBar.hide()
						UIHelper.makeEnableUI(true, container)
						UIHelper.toastInternetConnectionError(this, storeData.webStatus)
					}
				}
			}
		})

		//Наблюдаем за обновлением карточки на сервере
		cardModel.updateLiveData.observe(this, { storeData ->
			when(storeData.taskStatus) {
				TaskStatus.PROCESSING -> {
					progressBar.show()
					UIHelper.makeEnableUI(false, container)
				}
				//Если отправка на сервер прошла успешно
				TaskStatus.COMPLETED -> {
					progressBar.hide()
					UIHelper.makeEnableUI(true, container)

					//Обновляем полученную карточку локально
					storeData.data?.apply {
						currentCard!!.status = if(this.isValid) CardStatus.ACTIVE else CardStatus.EXPIRED
						cardModel.updateToDB(currentCard!!)
					} ?: run {
						currentCard!!.status = CardStatus.EXPIRED
						cardModel.updateToDB(currentCard!!)
					}
				}
				TaskStatus.CANCELED -> {
					//При устаревшем токене - пробуем обновить его и отправить запрос заново
					if(storeData.webStatus == WebStatus.UNAUTHORIZED) {
						cardModel.throughRefreshToken(this, account) { newAccount ->
							cardModel.updateToServer(currentCard!!, newAccount)
						}
					} else {
						progressBar.hide()
						UIHelper.makeEnableUI(true, container)
						UIHelper.toastInternetConnectionError(this, storeData.webStatus)
					}
				}
			}
		})

		//Наблюдаем за процессом работы локального обновления
		cardModel.localUpdatedCard.observe(this, { storeData ->
			if(storeData) {
				setResult(RESULT_OK, Intent().putExtra(NAME_EXTRA, currentCard))
				this.finish()
			} else {
				Toast.makeText(this, "Cannot update card.", Toast.LENGTH_SHORT).show()
				Log.e("CREATE CARD", "Could not update card locally.")
			}
		})

		//Наблюдаем за процессом локального добавления
		cardModel.localAddedCard.observe(this, { storeData ->
			if(storeData != null) {
				currentCard!!.cardId = storeData
				setResult(RESULT_OK, Intent().putExtra(NAME_EXTRA, currentCard))
				this.finish()
			} else {
				Toast.makeText(this, "Cannot save card.", Toast.LENGTH_SHORT).show()
				Log.e("CREATE CARD", "Could not save card locally.")
			}
		})

		//Обработчик нажатия на кнопку сохранения карточки в черновик
		draftButton.setOnClickListener {
			if(!assertFieldsValid(true)) return@setOnClickListener

			if(bundle != null) {
				//Обновляем старую
				val currentCard = Card(cardId = bundle!!.cardId,
									   title = titleText.text.toString().trim(),
									   content = contentText.text.toString().trim().takeUnless { it.isBlank() },
									   city = cityAutoText.text.toString().trim(),
									   faculty = facultiesAutoText.text.toString().trim().takeUnless { it.isBlank() },
									   speciality = specialitiesAutoText.text.toString().trim().takeUnless { it.isBlank() },
									   created = bundle!!.created,
									   status = bundle!!.status)

				//Обновляем карточку локально
				cardModel.updateToDB(currentCard)
			} else {
				//Создаём новую
				currentCard = Card(title = titleText.text.toString().trim(),
								   content = contentText.text.toString().trim().takeUnless { it.isBlank() },
								   city = cityAutoText.text.toString().trim(),
								   faculty = facultiesAutoText.text.toString().trim().takeUnless { it.isBlank() },
								   speciality = specialitiesAutoText.text.toString().trim().takeUnless { it.isBlank() },
								   created = LocalDate.now(),
								   status = CardStatus.DRAFT)

				//Отправляем карточку в базу
				cardModel.addToDB(currentCard!!)
			}
		}

		//Обработчик нажатия на кнопку обновления\создания карточки(отправка на сервер)
		createButton.setOnClickListener {
			if(!assertFieldsValid(true)) return@setOnClickListener

			if(bundle != null) {
				//Обновляем имеющуюся
				currentCard = Card(cardId = bundle!!.cardId,
								   title = titleText.text.toString().trim(),
								   content = contentText.text.toString().trim().takeUnless { it.isBlank() },
								   city = cityAutoText.text.toString().trim(),
								   faculty = facultiesAutoText.text.toString().trim().takeUnless { it.isBlank() },
								   speciality = specialitiesAutoText.text.toString().trim().takeUnless { it.isBlank() },
								   created = bundle!!.created,
								   status = bundle!!.status)

				//Обновляем на сервере
				if(currentCard!!.status == CardStatus.ACTIVE) {
					currentCard!!.id = bundle!!.id
					cardModel.updateToServer(currentCard!!, account)
				} else if(currentCard!!.status == CardStatus.DRAFT) //Отправляем на сервер
					cardModel.sendToServer(currentCard!!, account)
			} else {
				//Создаём новую
				currentCard = Card(title = titleText.text.toString().trim(),
								   content = contentText.text.toString().trim(),
								   city = cityAutoText.text.toString().trim(),
								   faculty = facultiesAutoText.text.toString().trim(),
								   speciality = specialitiesAutoText.text.toString().trim(),
								   created = LocalDate.now(),
								   status = CardStatus.ACTIVE)

				//Сначала пробуем отправить на сервер
				cardModel.sendToServer(currentCard!!, account)
			}
		}
	}

	override fun onBackPressed() {
		//Показываем оповещение, что карточку можно сохранить
		if(assertFieldsValid(false) && bundle == null) {
			val alertDialog: AlertDialog? = MaterialAlertDialogBuilder(this).apply {
				setTitle(R.string.title_attention)
				setMessage(R.string.alert_cancel_creating_card)
				setPositiveButton(R.string.bt_save) { dialog: DialogInterface, id: Int ->
					//Обращаемся к нажатию на кнопку сохранения в черновик
					draftButton.callOnClick()
				}
				setNeutralButton(R.string.bt_cancel) { dialog: DialogInterface, i: Int ->
					//Просто закрываем диалоговое окно
					dialog.cancel()
				}
				setNegativeButton(R.string.bt_dont_save) { dialog: DialogInterface, i: Int ->
					//Выходим из странице без сохранения
					dialog.dismiss()
					super.onBackPressed()
				}
			}.create()
			alertDialog?.show()
		}
		else super.onBackPressed()
	}

	/**
	 * Утверждает, валидны ли поля заполнения и, если нет, возвращает `false`
	 * @param setErrors Устанавливает подсказки под каждым, если где неверно.
	 */
	private fun assertFieldsValid(setErrors: Boolean): Boolean {
		var isValid: Boolean = true

		@ColorRes
		val errorColor: Int = TypedValue().also { theme.resolveAttribute(R.attr.colorError, it, true) }.data
		val hintColor: Int = getColor(android.R.color.darker_gray)

		if(titleText.text.isNullOrBlank()) {
			if(setErrors) titleText.setHintTextColor(errorColor)
			isValid = false
		} else titleText.setHintTextColor(hintColor)

		if(cityAutoText.text.isNullOrBlank()) {
			if(setErrors) cityAutoText.setHintTextColor(errorColor)
			isValid = false
		} else cityAutoText.setHintTextColor(hintColor)

		//Показываем сообщение, что не все поля заполнены
		if(!isValid && setErrors) Toast.makeText(this, getString(R.string.error_fill_fields), Toast.LENGTH_SHORT).show()
		return isValid
	}

	companion object {
		public const val NAME_EXTRA: String = "CARD"
	}
}