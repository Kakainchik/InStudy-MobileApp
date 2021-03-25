package kz.gaudeamus.instudy

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.edit
import androidx.core.widget.ContentLoadingProgressBar
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import kz.gaudeamus.instudy.entities.UpdateStudentRequest
import kz.gaudeamus.instudy.models.HttpTask.*
import kz.gaudeamus.instudy.models.SettingsViewModel

class ChangeStudentContactActivity : AppCompatActivity() {
	//Визуальные компоненты
	private lateinit var nameText: EditText
	private lateinit var surnameText: EditText
	private lateinit var phoneText: EditText
	private lateinit var citySwitch: SwitchMaterial
	private lateinit var cityAutoText: AutoCompleteTextView
	private lateinit var saveButton: MaterialButton
	private lateinit var container: ScrollView
	private lateinit var progressBar: ContentLoadingProgressBar
	private lateinit var settings: SharedPreferences

	private val settingsModel: SettingsViewModel by viewModels()

	private val name: String?
		get() = if(nameText.text.trim().toString() != settings.getString("NAME", null)) nameText.text.trim().toString() else null
	private val surname: String?
		get() = if(surnameText.text.trim().toString() != settings.getString("SURNAME", null)) surnameText.text.trim().toString() else null
	private val phone: String?
		get() = if(phoneText.text.trim().toString() != settings.getString("PHONE", null)) phoneText.text.trim().toString() else null
	private val defaultCity: String?
		get() = if(citySwitch.isChecked) cityAutoText.text.trim().toString() else null
	private val hasOnlineChanges: Boolean
		get() = name != null && surname != null && phone != null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_change_student_contact)

		//Инициализируем визуальные компоненты
		nameText = findViewById(R.id.change_student_name_text)
		surnameText = findViewById(R.id.change_student_surname_text)
		phoneText = findViewById(R.id.change_student_phone_text)
		citySwitch = findViewById(R.id.change_student_default_city_switch)
		cityAutoText = findViewById(R.id.change_student_default_city_text)
		saveButton = findViewById(R.id.change_student_save_button)
		container = findViewById(R.id.change_student_container)
		progressBar = findViewById(R.id.progressbar)

		cityAutoText.setAdapter(ArrayAdapter.createFromResource(this,
																R.array.cities,
																android.R.layout.simple_spinner_item))

		val currentAccount = IOFileHelper.anyAccountOrNull(this)!!
		settings = getSharedPreferences("PERSONAL_DATA_KEY", Context.MODE_PRIVATE)

		nameText.setText(settings.getString("NAME", null))
		surnameText.setText(settings.getString("SURNAME", null))
		phoneText.setText(settings.getString("PHONE", null))
		citySwitch.isChecked = settings.contains("DEFAULT_CITY").also {
			if(it) {
				cityAutoText.visibility = View.VISIBLE
				cityAutoText.setText(settings.getString("DEFAULT_CITY", null))
			}
		}


		//Обработчик нажатия на смену состояния переключателя
		citySwitch.setOnCheckedChangeListener { _, isChecked ->
			cityAutoText.visibility = if(isChecked) View.VISIBLE else View.GONE
		}

		//Обработчик нажатия на кнопку сохранения настроек
		saveButton.setOnClickListener {
			//Проверяем, валидны ли некоторые поля
			var isValid: Boolean = true

			nameText.takeIf { it.text.isBlank() }
				?.run {
					error = getString(R.string.error_empty_text)
					isValid = false
				} ?: run { nameText.error = null }

			phoneText.takeIf { it.text.isBlank() }
				?.run {
					error = getString(R.string.error_empty_text)
					isValid = false
				} ?: run { phoneText.error = null }

			if(isValid) {
				//Сначала сохраняем локальные настройки, которые не отправляются на сервер
				settings.edit {
					if(defaultCity == null) remove("DEFAULT_CITY")
					else putString("DEFAULT_CITY", defaultCity)
				}

				//Если совершенно нет изменений, то можем не отправлять запрос
				if(!hasOnlineChanges) return@setOnClickListener

				val data = UpdateStudentRequest(name = name,
												surname = surname,
												phone = phone)

				//Наблюдаем за процессом отправки данных на сервер
				settingsModel.updateAccountLiveData.observe(this, { storeData ->
					when(storeData.taskStatus) {
						//Задача запущена
						TaskStatus.PROCESSING -> {
							progressBar.show()
							UIHelper.makeEnableUI(false, container)
						}
						//Контакты изменены успешно
						TaskStatus.COMPLETED -> {
							progressBar.hide()
							UIHelper.makeEnableUI(true, container)
							Toast.makeText(this, R.string.notice_account_updated, Toast.LENGTH_SHORT)
								.show()

							//Сохраняем локально
							settings.edit {
								if(name != null) putString("NAME", name)
								putString("SURNAME", surname)
								if(phone != null) putString("PHONE", phone)
							}
						}
						//Ошибка
						TaskStatus.CANCELED -> {
							//При устаревшем токене - пробуем обновить его и отправить запрос заново
							if(storeData.webStatus == WebStatus.UNAUTHORIZED) {
								settingsModel.throughRefreshToken(this, currentAccount) { newAccount ->
									settingsModel.updateStudent(newAccount, data)
								}
							} else {
								UIHelper.makeEnableUI(true, container)
								progressBar.hide()
								UIHelper.toastInternetConnectionError(this, storeData.webStatus)
							}
						}
					}
				})

				//Отправляем запрос на сервер
				settingsModel.updateStudent(currentAccount, data)
			}
		}
	}

	companion object {
		internal const val NAME = "CHANGE_CONTACT"
	}
}