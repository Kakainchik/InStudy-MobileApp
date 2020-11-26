package kz.gaudeamus.instudy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SignUpStudentFragment : Fragment() {

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
        //TODO: Проверять фамилию или нет?
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

            if(isValid) {
                //TODO("Registration")
            }

            isValid = true
        }

        return view
    }
}