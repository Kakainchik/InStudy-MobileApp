package kz.gaudeamus.instudy

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.lang.ClassCastException

class SignInFragment : Fragment(), PopupMenu.OnMenuItemClickListener {

    var loginInFragmentListener: OnLoginInFragmentInteractionListener? = null

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

            //Верный ли адрес
            emailText.takeUnless {
                android.util.Patterns.EMAIL_ADDRESS.matcher(it.text.toString()).matches()
            }?.run { emailLayout.error = view.resources.getText(R.string.error_invalid_email) }
                ?: run { emailLayout.error = null }


        }

        return view
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.signup_student_menu -> {
                //Переход на регистрацию студента
                this.loginInFragmentListener?.OnFragmentInteraction(LoginInActivity.KindaFragment.SIGN_UP_STUDENT)
            }
            R.id.signup_school_menu -> {
                //Переход на регистрацию школы
                this.loginInFragmentListener?.OnFragmentInteraction(LoginInActivity.KindaFragment.SIGN_UP_SCHOOL)
            }
        }

        return true
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            this.loginInFragmentListener = context as OnLoginInFragmentInteractionListener
        } catch(ex: ClassCastException) {
            ex.printStackTrace()
        }
    }
}