package kz.gaudeamus.instudy

import android.content.Context
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.ContentLoadingProgressBar
import com.google.android.material.button.MaterialButton
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
	private lateinit var container: ConstraintLayout
	private lateinit var settings: SharedPreferences

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
		val bundle = intent.getSerializableExtra(NAME_EXTRA) as? Card
		//Заполняем поля, если редактируем карточку, а не создаём новую
		bundle?.apply {
			draftButton.visibility = View.GONE
			if(status == CardStatus.EXPIRED) {
				createButton.visibility = View.GONE
				titleText.isEnabled = false
				contentText.isEnabled = false
				cityAutoText.isEnabled = false
				facultiesAutoText.isEnabled = false
				specialitiesAutoText.isEnabled = false
			}

			titleText.setText(title)
			contentText.setText(content)
			createButton.text = getString(R.string.bt_save)
			cityAutoText.setText(city)
			facultiesAutoText.setText(faculty)
			specialitiesAutoText.setText(speciality)
		}

		cityAutoText.apply {
			this.setAdapter(ArrayAdapter.createFromResource(this@CreateCardActivity,
															R.array.cities,
															android.R.layout.simple_spinner_item))
			if(bundle == null) this.adapter.getItem(0) as String
			if(settings.contains("DEFAULT_CITY")) this.setText(settings.getString("DEFAULT_CITY", null))
		}

		//Обработчик нажатия на кнопку сохранения карточки в черновик
		draftButton.setOnClickListener {
			if(!assertFieldsValid()) return@setOnClickListener

			val card = Card(title = titleText.text.toString().trim(),
							content = contentText.text.toString().trim(),
							city = cityAutoText.text.toString().trim(),
							faculty = facultiesAutoText.text.toString().trim(),
							speciality = specialitiesAutoText.text.toString().trim(),
							created = LocalDate.now(),
							status = CardStatus.DRAFT)

			//Наблюдаем за процессом работы
			cardModel.localAddedCard.observe(this, { storeData ->
				if(storeData != null) {
					card.cardId = storeData
					setResult(RESULT_OK, Intent().putExtra(NAME_EXTRA, card))
					this.finish()
				} else {
					Toast.makeText(this, "Cannot save card.", Toast.LENGTH_SHORT).show()
					Log.e("CREATE CARD", "Could not save card locally.")
				}
			})

			//Отправляем карточку в базу
			cardModel.addToDB(card)
		}

		//Обработчик нажатия на кнопку обновления\создания карточки(отправка на сервер)
		createButton.setOnClickListener {
			if(!assertFieldsValid()) return@setOnClickListener

			if(bundle != null) {
				//Обновляем имеющуюся
				val card = Card(cardId = bundle.cardId,
								title = titleText.text.toString().trim(),
								content = contentText.text.toString().trim(),
								city = cityAutoText.text.toString().trim(),
								faculty = facultiesAutoText.text.toString().trim(),
								speciality = specialitiesAutoText.text.toString().trim(),
								created = bundle.created,
								status = bundle.status,
								id = bundle.id)

				//Наблюдаем за процессом работы локального обновления
				cardModel.localUpdatedCard.observe(this, { storeData ->
					if(storeData) {
						setResult(RESULT_OK, Intent().putExtra(NAME_EXTRA, card))
						this.finish()
					} else {
						Toast.makeText(this, "Cannot update card.", Toast.LENGTH_SHORT).show()
						Log.e("CREATE CARD", "Could not update card locally.")
					}
				})

				//Отправляем обновлённую карточку на сервер, если статус активные, иначе в черновик
				if(bundle.status == CardStatus.DRAFT) cardModel.updateToDB(card)
				else {
					val requestData = UpdateCardRequest(id = card.id!!,
														title = card.title,
														content = card.content,
														soughtCity = card.city,
														faculty = card.faculty,
														speciality = card.speciality)

					//Наблюдаем за отправлением карточки на сервер
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
									card.status = if(this.isValid) CardStatus.ACTIVE else CardStatus.EXPIRED
									cardModel.updateToDB(card)
								} ?:run {
									card.status = CardStatus.EXPIRED
									cardModel.updateToDB(card)
								}
							}
							TaskStatus.CANCELED -> {
								//При устаревшем токене - пробуем обновить его и отправить запрос заново
								if(storeData.webStatus == WebStatus.UNAUTHORIZED) {
									cardModel.throughRefreshToken(this, account) { newAccount ->
										cardModel.updateToServer(requestData, newAccount)
									}
								} else {
									progressBar.hide()
									UIHelper.makeEnableUI(true, container)
									UIHelper.toastInternetConnectionError(this, storeData.webStatus)
								}
							}
						}
					})

					cardModel.updateToServer(requestData, account)
				}
			} else {
				//Создаём новую
				val card = Card(title = titleText.text.toString().trim(),
								content = contentText.text.toString().trim(),
								city = cityAutoText.text.toString().trim(),
								faculty = facultiesAutoText.text.toString().trim(),
								speciality = specialitiesAutoText.text.toString().trim(),
								created = LocalDate.now(),
								status = CardStatus.ACTIVE)

				val requestData = AddCardRequest(title = card.title,
												 content = card.content,
												 soughtCity = card.city,
												 faculty = card.faculty,
												 speciality = card.speciality)

				//Наблюдаем за процессом работы локального добавления
				cardModel.localAddedCard.observe(this, { storeData ->
					if(storeData != null) {
						card.cardId = storeData
						setResult(RESULT_OK, Intent().putExtra(NAME_EXTRA, card))
						this.finish()
					} else {
						Toast.makeText(this, "Cannot save card.", Toast.LENGTH_SHORT).show()
						Log.e("CREATE CARD", "Could not save card locally.")
					}
				})

				//Наблюдаем за процессом работы отправки на сервер
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
									card.id = this.id
									card.status =
										if(this.isValid) CardStatus.ACTIVE else CardStatus.EXPIRED
									cardModel.addToDB(card)
							}
						}
						TaskStatus.CANCELED -> {
							//При устаревшем токене - пробуем обновить его и отправить запрос заново
							if(storeData.webStatus == WebStatus.UNAUTHORIZED) {
								cardModel.throughRefreshToken(this, account) { newAccount ->
									cardModel.sendToServer(requestData, newAccount)
								}
							} else {
								progressBar.hide()
								UIHelper.makeEnableUI(true, container)
								UIHelper.toastInternetConnectionError(this, storeData.webStatus)
							}
						}
					}
				})
				//Сначала пробуем отправить на сервер
				cardModel.sendToServer(requestData, account)
			}
		}
	}

	override fun onBackPressed() {
		super.onBackPressed()
		//TODO: Всплывающий диалог на сохранение карточки
	}

	/**
	 * Утверждает, валидны ли поля заполнения и, если нет, возвращает `false` и устанавливает подсказку под каждым.
	 */
	private fun assertFieldsValid(): Boolean {
		var isValid: Boolean = true
		@ColorRes
		val errorColor: Int = TypedValue().also { theme.resolveAttribute(R.attr.colorError, it, true) }.data
		val hintColor: Int = getColor(android.R.color.darker_gray)

		if(titleText.text.isNullOrBlank()) {
			titleText.setHintTextColor(errorColor)
			isValid = false
		} else titleText.setHintTextColor(hintColor)

		if(cityAutoText.text.isNullOrBlank()) {
			cityAutoText.setHintTextColor(errorColor)
			isValid = false
		} else cityAutoText.setHintTextColor(hintColor)

		if(facultiesAutoText.text.isNullOrBlank()) {
			facultiesAutoText.setHintTextColor(errorColor)
			isValid = false
		} else facultiesAutoText.setHintTextColor(hintColor)

		if(specialitiesAutoText.text.isNullOrBlank()) {
			specialitiesAutoText.setHintTextColor(errorColor)
			isValid = false
		} else specialitiesAutoText.setHintTextColor(hintColor)

		//Показываем сообщение, что не все поля заполнены
		if(!isValid) Toast.makeText(this, getString(R.string.error_fill_fields), Toast.LENGTH_SHORT).show()
		return isValid
	}

	companion object {
		public const val NAME_EXTRA: String = "CARD"
		public const val ACTIVITY_CODE = 101
	}
}