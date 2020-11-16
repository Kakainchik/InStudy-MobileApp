package kz.gaudeamus.instudy

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SignUpSchoolFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sign_up_school, container, false)

        //Визуальные компоненты
        val signUpButton: MaterialButton = view.findViewById(R.id.signup_school_register_button)
        val emailText: TextInputEditText = view.findViewById(R.id.signup_school_email_text)
        val emailLayout: TextInputLayout = view.findViewById(R.id.signup_school_email_input)
        val passwordText: TextInputEditText = view.findViewById(R.id.signup_school_password_text)
        val passwordLayout: TextInputLayout = view.findViewById(R.id.signup_school_password_input)
        val repassText: TextInputEditText = view.findViewById(R.id.signup_school_repassword_text)
        val repassLayout: TextInputLayout = view.findViewById(R.id.signup_school_repassword_input)
        val organizationText: TextInputEditText = view.findViewById(R.id.signup_school_name_text)
        val organizationLayout: TextInputLayout = view.findViewById(R.id.signup_school_name_input)

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
            organizationText.takeUnless { isNameOrganizationValid(it.text.toString().trim()) }
                ?.run {
                    organizationLayout.error = view.resources.getString(R.string.error_counter_minimum)
                    isValid = false
                }

            if(isValid) {
                //TODO("Registration")
            }

            isValid = true
        }

        //Выводим ошибку если сверхлимит длины
        organizationText.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //NOTHING
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                organizationLayout.takeIf { s?.toString()?.length!! > it.counterMaxLength }
                    ?.run {
                        this.error = view.resources.getString(R.string.error_counter_limit)
                        isValid = false
                    }
                    ?: run { organizationLayout.error = null }
            }

            override fun afterTextChanged(s: Editable?) {
                //Если ошибки нет
                organizationLayout.error?:run { isValid = true }
            }
        })

        return view
    }

    /**
     * <p>  Current pattern: minimum 3 character,
     *      should contain at least 1 letter </p>
     * @param name should be trimmed
     * @return `true` if current name matches the matcher's pattern
     */
    private fun isNameOrganizationValid(name: String?): Boolean {
        name?.let {
            val namePattern = """^(?=.+\D).{3,}$"""
            val nameMatcher = Regex(namePattern)

            return nameMatcher.find(name) != null
        } ?: return false
    }
}