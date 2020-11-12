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
import java.lang.ClassCastException

class SignInFragment : Fragment(), PopupMenu.OnMenuItemClickListener {

    var loginInFragmentListener: OnLoginInFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sign_in, container, false)

        //Визуальные компоненты
        val signUpButton: MaterialButton = view.findViewById(R.id.signin_register_button)

        signUpButton.setOnClickListener { view ->
            val popup = PopupMenu(context, view)
            popup.menuInflater.inflate(R.menu.sign_up_type_menu, popup.menu)

            //Обработчик нажатия на элемент меню
            popup.setOnMenuItemClickListener(this)

            popup.show()
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