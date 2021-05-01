package kz.gaudeamus.instudy

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kz.gaudeamus.instudy.entities.AccountKind
import kz.gaudeamus.instudy.entities.RegistrationStudentRequest
import kz.gaudeamus.instudy.models.HttpTask.*
import kz.gaudeamus.instudy.models.RegistrationStudentViewModel
import java.lang.ClassCastException

class SignUpStudentFragment : Fragment() {

    private val model: RegistrationStudentViewModel by activityViewModels()
    private var loginInFragmentListener: OnLoginInFragmentListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sign_up_student, container, false)

        //Визуальные компоненты
        val signUpButton: MaterialButton = view.findViewById(R.id.signup_student_register_button)
        val emailText: TextInputEditText = view.findViewById(R.id.signup_student_email_text)
        val emailLayout: TextInputLayout = view.findViewById(R.id.signup_student_email_input)
        val passwordText: TextInputEditText = view.findViewById(R.id.signup_student_password_text)
        val passwordLayout: TextInputLayout = view.findViewById(R.id.signup_student_password_input)
        val repassText: TextInputEditText = view.findViewById(R.id.signup_student_repassword_text)
        val repassLayout: TextInputLayout = view.findViewById(R.id.signup_student_repassword_input)
        val phoneText: TextInputEditText = view.findViewById(R.id.signup_student_phone_text)
        val phoneLayout: TextInputLayout = view.findViewById(R.id.signup_student_phone_input)
        val nameText: TextInputEditText = view.findViewById(R.id.signup_student_name_text)
        val nameLayout: TextInputLayout = view.findViewById(R.id.signup_student_name_input)
        val surnameText: TextInputEditText = view.findViewById(R.id.signup_student_surname_text)
        val surnameLayout: TextInputLayout = view.findViewById(R.id.signup_student_surname_input)

        //Переменные
        var isValid: Boolean = true

        //Обработчик нажатия на кнопку регистрации
        signUpButton.setOnClickListener {
            //Верный ли адрес
            emailText.takeUnless {
                android.util.Patterns.EMAIL_ADDRESS.matcher(it.text.toString()).matches()
            }?.run {
                emailLayout.error = view.resources.getString(R.string.error_invalid_email)
                isValid = false
            } ?:run { emailLayout.error = null }

            //Верный ли пароль
            passwordText.takeUnless { isPasswordValid(it.text.toString()) }
                ?.run {
                    passwordLayout.error = view.resources.getString(R.string.error_unmatched_password)
                    isValid = false
                } ?:run { passwordLayout.error = null }

            //Верен ли повторный пароль
            repassText.takeUnless { it.text.toString().equals(passwordText.text.toString()) }
                ?.run {
                    repassLayout.error = view.resources.getString(R.string.error_invalid_repassword)
                    isValid = false
                } ?:run { repassLayout.error = null }

            //Не пустое ли имя
            nameText.takeIf { it.text.isNullOrEmpty() }
                ?.run {
                    nameLayout.error = view.resources.getString(R.string.error_empty_text)
                    isValid = false
                } ?:run { nameLayout.error = null }

            //Не пустой ли номер
            phoneText.takeIf { it.text.isNullOrEmpty() }
                ?.run {
                    phoneLayout.error = view.resources.getString(R.string.error_empty_text)
                    isValid = false
                } ?:run { phoneLayout.error = null }

            //Все условия соблюдены - отправляем запрос
            if(isValid) {
                val data = RegistrationStudentRequest(email = emailText.text.toString(),
                                                      password = passwordText.text.toString(),
                                                      phone = phoneText.text.toString(),
                                                      name = nameText.text.toString(),
                                                      surname = surnameText.text.toString())

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

                            this.loginInFragmentListener?.onFragmentInteraction(LoginInActivity.KindaFragment.SIGN_IN)
                            this.loginInFragmentListener?.onRegistered(AccountKind.STUDENT)
                        }
                        TaskStatus.CANCELED -> {
                            //Операция отменена, разблокируем интерфейс и выводим сообщение
                            when(storeData.webStatus) {
                                WebStatus.METHOD_NOT_ALLOWED ->
                                    Toast.makeText(context, getText(R.string.error_user_email_exist), Toast.LENGTH_SHORT).show()
                                WebStatus.UNPROCESSABLE_ENTITY ->
                                    Toast.makeText(context, getText(R.string.error_phone_exist), Toast.LENGTH_SHORT).show()
                                else -> UIHelper.toastInternetConnectionError(requireContext(), storeData.webStatus)
                            }
                            this.loginInFragmentListener?.onBlockUI(true)
                        }
                    }
                })
                model.register(data)
            }

            isValid = true
        }

        return view
    }

    //Прикрепляем интерфейс слушатель
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            this.loginInFragmentListener = context as OnLoginInFragmentListener
        } catch(ex: ClassCastException) {
            ex.printStackTrace()
        }
    }
}