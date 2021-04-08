package kz.gaudeamus.instudy

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.activityViewModels
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kz.gaudeamus.instudy.entities.Account
import kz.gaudeamus.instudy.entities.AccountKind
import kz.gaudeamus.instudy.models.HttpTask
import kz.gaudeamus.instudy.models.HttpTask.*
import kz.gaudeamus.instudy.models.SettingsViewModel
import java.lang.ClassCastException

class SettingsFragment : Fragment() {
	private val model: SettingsViewModel by activityViewModels()

	/**
	 * Обработчик события на изменение пароля из [ChangePasswordActivity]
	 */
	private val changePasswordCallback: ActivityResultLauncher<Account> = registerForActivityResult(ChangePasswordActivityContract()) {
		if(it) Toast.makeText(context, getText(R.string.notice_password_updated), Toast.LENGTH_SHORT).show()
	}

	/**
	 * Обработчик события на изменение контактных данных из [ChangeContactActivity]
	 */
	private val changeContactCallback: ActivityResultLauncher<Account> = registerForActivityResult(ChangeContactActivityContract()) {

	}

	private lateinit var currentAccount: Account

	private var logoutListener: OnLogoutListener? = null
	private var settings: SharedPreferences? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		settings = activity?.getSharedPreferences("PERSONAL_DATA_KEY", Context.MODE_PRIVATE)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val view = inflater.inflate(R.layout.fragment_settings, container, false)

		//Визуальные компоненты
		val appBar: MaterialToolbar = view.findViewById(R.id.main_appbar)
		val fullnameText: TextView = view.findViewById(R.id.settings_fullname_text)
		val emailText: TextView = view.findViewById(R.id.settings_email_text)
		val passwordButton: MaterialButton = view.findViewById(R.id.settings_password_button)
		val logoutButton: MaterialButton = view.findViewById(R.id.settings_logout_button)
		val contactButton: MaterialButton = view.findViewById(R.id.settings_contact_button)

		//Весь фрагмент замешан на аккаунте
		currentAccount = IOFileHelper.anyAccountOrNull(requireContext())!!
		with(currentAccount.email) {
			val spannable = SpannableString(this)
			spannable.setSpan(StyleSpan(Typeface.ITALIC),
							  indexOf('@'),
							  length,
							  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
			emailText.setText(spannable)
		}
		if(currentAccount.kind == AccountKind.MODERATOR)
			contactButton.visibility = View.GONE

		settings?.run {
			if(currentAccount.kind == AccountKind.STUDENT) {
				val fullname = "${getString("NAME", "NAME")} ${getString("SURNAME", "")}"
				fullnameText.text = fullname
			}
			if(currentAccount.kind == AccountKind.SCHOOL) {
				val fullname = getString("ORGANIZATION", "ORGANIZATION")
				fullnameText.text = fullname
			}
		}

		//Обработчик нажатия на кнопку изменения контактных данных
		contactButton.setOnClickListener {
			changeContactCallback.launch(currentAccount)
		}

		//Обработчик нажатия на кнопку изменения пароля
		passwordButton.setOnClickListener {
			changePasswordCallback.launch(currentAccount)
		}

		//Обработчик нажатия на кнопку выхода из аккаунта
		logoutButton.setOnClickListener {
			settings?.edit()?.clear()?.apply()
			model.logout(currentAccount)
			logoutListener?.onLogout()
		}

		return view
	}

	override fun onAttach(context: Context) {
		super.onAttach(context)
		try {
			this.logoutListener = context as OnLogoutListener
		} catch(ex: ClassCastException) {
			ex.printStackTrace()
		}
	}

	/**
	 * Класс для перехода на страницу изменения пароля и получения итогового результата от неё.
	 */
	private class ChangePasswordActivityContract : ActivityResultContract<Account, Boolean>() {
		override fun createIntent(context: Context, input: Account?): Intent {
			return Intent(context, ChangePasswordActivity::class.java).apply {
				putExtra(ChangePasswordActivity.NAME, input)
			}
		}

		override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
			return resultCode == Activity.RESULT_OK
		}
	}

	/**
	 * Класс для перехода на страницу изменения контактных данных и получения итогового результата от неё.
	 */
	private class ChangeContactActivityContract : ActivityResultContract<Account, Boolean>() {
		override fun createIntent(context: Context, input: Account): Intent {
			return when(input.kind) {
				AccountKind.STUDENT -> Intent(context, ChangeStudentContactActivity::class.java).apply {
					putExtra(ChangeStudentContactActivity.NAME, input)
				}
				AccountKind.SCHOOL -> Intent(context, ChangeSchoolContactActivity::class.java).apply {
					putExtra(ChangeSchoolContactActivity.NAME, input)
				}
				AccountKind.MODERATOR -> TODO("There is no contacts to change")
			}
		}

		override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
			return resultCode == Activity.RESULT_OK
		}
	}
}