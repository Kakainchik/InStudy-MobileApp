package kz.gaudeamus.instudy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentTransaction

class LoginInActivity : AppCompatActivity(), OnLoginInFragmentInteractionListener {

    private val signInFragment: SignInFragment
    private val signUpSchoolFragment: SignUpSchoolFragment
    private val signUpStudentFragment: SignUpStudentFragment
    private var isBusy: Boolean
    private var currentFragment: KindaFragment

    init {
        signInFragment = SignInFragment()
        signUpSchoolFragment = SignUpSchoolFragment()
        signUpStudentFragment = SignUpStudentFragment()
        isBusy = false
        currentFragment = KindaFragment.SIGN_IN
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_in)

        //Если нет аккаунта
        if(savedInstanceState == null) {
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            transaction.add(R.id.loginin_fragment_container, signInFragment)
            transaction.commit() //Переходим на страницу авторизации
        }
    }

    override fun OnFragmentInteraction(fragment: KindaFragment) {
        when(fragment) {
            KindaFragment.SIGN_UP_STUDENT -> {
                val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.loginin_fragment_container, signUpStudentFragment)
                transaction.addToBackStack(null)
                transaction.commit() //Переходим на страницу регистрации студента
            }
            KindaFragment.SIGN_UP_SCHOOL -> {
                val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.loginin_fragment_container, signUpSchoolFragment)
                transaction.addToBackStack(null)
                transaction.commit() //Переходим на страницу регистрации школы
            }
        }
    }
}

public enum class KindaFragment {
    SIGN_IN,
    SIGN_UP_STUDENT,
    SIGN_UP_SCHOOL
}