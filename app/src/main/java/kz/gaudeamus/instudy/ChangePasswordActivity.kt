package kz.gaudeamus.instudy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.ContentLoadingProgressBar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kz.gaudeamus.instudy.entities.Account
import kz.gaudeamus.instudy.entities.UpdatePasswordRequest
import kz.gaudeamus.instudy.models.HttpTask
import kz.gaudeamus.instudy.models.HttpTask.*
import kz.gaudeamus.instudy.models.SettingsViewModel

class ChangePasswordActivity : AppCompatActivity() {
	private val model: SettingsViewModel by viewModels()

	//Визуальные компоненты
	private lateinit var oldPasswordLayout: TextInputLayout
	private lateinit var oldPasswordText: TextInputEditText
	private lateinit var newPasswordLayout: TextInputLayout
	private lateinit var newPasswordText: TextInputEditText
	private lateinit var confirmPasswordLayout: TextInputLayout
	private lateinit var confirmPasswordText: TextInputEditText
	private lateinit var updatePasswordButton: MaterialButton
	private lateinit var container: ScrollView
	private lateinit var progressBar: ContentLoadingProgressBar

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_change_password)

		//Инициализируем визуальные компоненты
		oldPasswordLayout = findViewById(R.id.settings_oldpass_input)
		oldPasswordText = findViewById(R.id.settings_oldpass_text)
		newPasswordLayout = findViewById(R.id.settings_newpass_input)
		newPasswordText = findViewById(R.id.settings_newpass_text)
		confirmPasswordLayout = findViewById(R.id.settings_confirmpass_input)
		confirmPasswordText = findViewById(R.id.settings_confirmpass_text)
		updatePasswordButton = findViewById(R.id.settings_updatepass_button)
		container = findViewById(R.id.change_password_container)
		progressBar = findViewById(R.id.progressbar)

		val bundle = intent.getSerializableExtra(NAME) as Account

		//Обработчик нажатия на кнопку обновления пароля
		updatePasswordButton.setOnClickListener {
			//Проверяем, все ли поля валидны для паролей
			var isValid: Boolean = true

			oldPasswordText.takeIf { it.text.toString().isBlank() }
				?.run {
					oldPasswordLayout.error = getString(R.string.error_empty_text)
					isValid = false
				} ?:run { oldPasswordLayout.error = null }

			newPasswordText.takeUnless { isPasswordValid(it.text.toString()) }
				?.run {
					newPasswordLayout.error = getString(R.string.error_unmatched_password)
					isValid = false
				} ?:run { newPasswordLayout.error = null }

			confirmPasswordText.takeUnless { it.text.toString().equals(newPasswordText.text.toString()) }
				?.run {
					confirmPasswordLayout.error = getString(R.string.error_invalid_repassword)
					isValid = false
				} ?: run { confirmPasswordLayout.error = null }

			//Если нет ошибок - отправляем запрос
			if(isValid) {
				val request = UpdatePasswordRequest(oldPassword =  oldPasswordText.text.toString(),
													newPassword = newPasswordText.text.toString())

				//Наблюдаем за ходом работы
				model.updatePassLiveData.observe(this, { storeData ->
					when(storeData.taskStatus) {
						//Задача запущена
						TaskStatus.PROCESSING -> {
							progressBar.show()
							UIHelper.makeEnableUI(false, container)
						}
						//Пароль сменён успешно
						TaskStatus.COMPLETED -> {
							progressBar.hide()
							UIHelper.makeEnableUI(true, container)
							this.setResult(RESULT_OK)
							this.finish()
						}
						//Ошибка
						TaskStatus.CANCELED -> {
							//При устаревшем токене - пробуем обновить его и отправить запрос заново
							if(storeData.webStatus == WebStatus.UNAUTHORIZED) {
								model.throughRefreshToken(this, bundle) { newAccount ->
									model.updatePassword(newAccount, request)
								}
							} else {
								UIHelper.makeEnableUI(true, container)
								progressBar.hide()

								if(storeData.webStatus == WebStatus.NONE)
									Toast.makeText(this, getString(R.string.error_settings_invalid_password), Toast.LENGTH_SHORT).show()
								else UIHelper.toastInternetConnectionError(this, storeData.webStatus)
							}
						}
					}
				})

				//Отправляем запрос
				model.updatePassword(bundle, request)
			}
		}
	}

	companion object {
		public const val NAME: String = "CHANGE_PASSWORD"
	}
}