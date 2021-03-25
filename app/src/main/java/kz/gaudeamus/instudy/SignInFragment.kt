package kz.gaudeamus.instudy

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kz.gaudeamus.instudy.entities.*
import kz.gaudeamus.instudy.models.AuthorizationViewModel
import kz.gaudeamus.instudy.models.HttpTask.*
import java.lang.ClassCastException
import java.time.LocalDate

class SignInFragment : Fragment(), PopupMenu.OnMenuItemClickListener {

    private val model: AuthorizationViewModel by activityViewModels()
    private var loginInFragmentListener: OnLoginInFragmentListener? = null
    private var settings: SharedPreferences? = null

    private lateinit var user: Account

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settings = activity?.getSharedPreferences("PERSONAL_DATA_KEY", Context.MODE_PRIVATE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sign_in, container, false)

        //Визуальные компоненты
        val signUpButton: MaterialButton = view.findViewById(R.id.signin_register_button)
        val signInButton: MaterialButton = view.findViewById(R.id.signin_login_button)
        val emailText: TextInputEditText = view.findViewById(R.id.signin_email_text)
        val emailLayout: TextInputLayout = view.findViewById(R.id.signin_email_input)
        val passwordText: TextInputEditText = view.findViewById(R.id.signin_password_text)
        val passwordLayout: TextInputLayout = view.findViewById(R.id.signin_password_input)

        //Обработчик нажатия на кнопку регистрации
        signUpButton.setOnClickListener {
            val popup = PopupMenu(context, it)
            popup.menuInflater.inflate(R.menu.sign_up_type_menu, popup.menu)

            //Обработчик нажатия на элемент меню
            popup.setOnMenuItemClickListener(this)

            popup.show()
        }

        //Обработчик нажатия на кнопку входа
        signInButton.setOnClickListener {
            var isValid: Boolean = true

            //Верный ли адрес
            emailText.takeUnless {
                android.util.Patterns.EMAIL_ADDRESS.matcher(it.text.toString()).matches()
            }?.run {
                emailLayout.error = resources.getText(R.string.error_invalid_email)
                isValid = false
            } ?:run { emailLayout.error = null }

            //Не пустое ли поле ввода пароля
            passwordText.takeIf { it.text.isNullOrEmpty() }
                ?.run {
                    passwordLayout.error = resources.getString(R.string.error_empty_text)
                    isValid = false
                } ?:run { passwordLayout.error = null }

            model.receivedPersonalInformation.observe(this, { storeData ->
                when(storeData) {
                    is SchoolInformationResponse -> {
                        settings?.edit {
                            putString("ORGANIZATION", storeData.organization)
                            putString("EXPIRES", storeData.licenseExpiresAt)
                            loginInFragmentListener?.onAuthorized(user)
                        }
                    }
                    is StudentInformationResponse -> {
                        settings?.edit {
                            putString("NAME", storeData.name)
                            putString("SURNAME", storeData.surname)
                            putString("PHONE", storeData.phone)
                            loginInFragmentListener?.onAuthorized(user)
                        }
                    }
                    null -> {
                        //NOTHING
                    }
                }
            })

            //Все условия соблюдены - отправляем запрос
            if(isValid) {
                val data = AuthorizationRequest(email = emailText.text.toString(),
                                                password = passwordText.text.toString())

                //Наблюдаем за изменениями процесса работы
                model.signinLiveData.observe(this.viewLifecycleOwner, { storeData ->
                    val resource = storeData?.data

                    when(storeData.taskStatus) {
                        TaskStatus.PROCESSING -> {
                            //Операция в процессе, блокируем интерфейс
                            this.loginInFragmentListener?.onBlockUI(false)
                        }
                        TaskStatus.COMPLETED -> {
                            //Операция завершена, разблокируем интерфейс
                            this.loginInFragmentListener?.onBlockUI(true)

                            //Возвращаемся в main menu
                            resource?.let {
                                if(!resource.isVerified)
                                    Toast.makeText(context, R.string.error_sign_in_invalid_login_or_pass, Toast.LENGTH_LONG).show()
                                else {
                                    user = Account(id = resource.id,
                                                       email = resource.email,
                                                       token = resource.token!!,
                                                       refreshToken = resource.refreshToken!!,
                                                       kind = AccountKind.from(resource.role))

                                    if(user.kind == AccountKind.MODERATOR) this.loginInFragmentListener?.onAuthorized(user)
                                    else model.receivePersonalInformation(user)
                                }
                            } ?:run {
                                //Если по каким-то причинам нет данных
                                Toast.makeText(context,
                                               resources.getText(R.string.error_unexpected),
                                               Toast.LENGTH_SHORT).show()
                            }
                        }
                        TaskStatus.CANCELED -> {
                            //Операция отменена, разблокируем интерфейс и выводим сообщение
                            this.loginInFragmentListener?.onBlockUI(true)
                            if(storeData.webStatus == WebStatus.NONE)
                                Toast.makeText(requireContext(), getString(R.string.error_sign_in_invalid_login_or_pass), Toast.LENGTH_SHORT).show()
                            else UIHelper.toastInternetConnectionError(requireContext(), storeData.webStatus)
                        }
                    }
                })
                model.authorize(data)
            }
        }

        return view
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.signup_student_menu -> {
                //Переход на регистрацию студента
                this.loginInFragmentListener?.onFragmentInteraction(LoginInActivity.KindaFragment.SIGN_UP_STUDENT)
            }
            R.id.signup_school_menu -> {
                //Переход на регистрацию школы
                this.loginInFragmentListener?.onFragmentInteraction(LoginInActivity.KindaFragment.SIGN_UP_SCHOOL)
            }
        }

        return true
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            this.loginInFragmentListener = context as OnLoginInFragmentListener
        } catch(ex: ClassCastException) {
            ex.printStackTrace()
        }
    }
}