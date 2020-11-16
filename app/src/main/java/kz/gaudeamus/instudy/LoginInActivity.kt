package kz.gaudeamus.instudy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentTransaction

/**
 * <p>  Current pattern: minimum 7 character,
 *      should contain at least 1 digit, 1 uppercase, 1 lowercase </p>
 * @return `true` if current password matches the matcher's pattern
 */
public fun isPasswordValid(password: String?): Boolean {
    password?.let {
        val passwordPattern = """^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?!.*\s).{7,}$"""
        val passwordMatcher = Regex(passwordPattern)

        return passwordMatcher.find(it) != null
    } ?: return false
}

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
            transaction.commit() //Переходим на фрагмент авторизации
        }
    }

    override fun OnFragmentInteraction(fragment: KindaFragment) {
        when(fragment) {
            KindaFragment.SIGN_UP_STUDENT -> {
                val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.loginin_fragment_container, signUpStudentFragment)
                transaction.addToBackStack(null)
                transaction.commit() //Переходим на фрагмент регистрации студента
            }
            KindaFragment.SIGN_UP_SCHOOL -> {
                val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.loginin_fragment_container, signUpSchoolFragment)
                transaction.addToBackStack(null)
                transaction.commit() //Переходим на фрагмент регистрации школы
            }
        }
    }

    public enum class KindaFragment {
        SIGN_IN,
        SIGN_UP_STUDENT,
        SIGN_UP_SCHOOL
    }
}