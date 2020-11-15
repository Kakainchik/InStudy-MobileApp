package kz.gaudeamus.instudy

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.TelephonyManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
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

        //Обработчик нажатия на кнопку регистрации
        signUpButton.setOnClickListener {
            //Верный ли адрес
            emailText.takeUnless {
                android.util.Patterns.EMAIL_ADDRESS.matcher(it.text.toString()).matches()
            }?.run { emailLayout.error = view.resources.getString(R.string.error_invalid_email) }
                ?: run { emailLayout.error = null }

            //Верный ли пароль
            passwordText.takeUnless { isPasswordValid(it.text.toString()) }
                ?.run { passwordLayout.error = view.resources.getString(R.string.error_unmatched_password) }
                ?: run { passwordLayout.error = null }

            //Верен ли повторный пароль
            repassText.takeUnless { it.text.toString().equals(passwordText.text.toString()) }
                ?.run { repassLayout.error = view.resources.getString(R.string.error_invalid_repassword) }
                ?: run { repassLayout.error = null }
        }

        return view
    }
}