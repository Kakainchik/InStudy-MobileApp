package kz.gaudeamus.instudy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.commit
import kz.gaudeamus.instudy.UIHelper.makeEnableUI
import kz.gaudeamus.instudy.entities.Account
import kz.gaudeamus.instudy.entities.AccountKind

/**
 * <p>  Current pattern: minimum 7 character,
 *      should contain at least 1 digit, 1 uppercase, 1 lowercase </p>
 * @return `true` if current password matches the matcher's pattern
 */
public fun isPasswordValid(password: String?): Boolean {
    return password?.let {
        val passwordPattern = """^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?!.*\s).{7,}$"""
        val passwordMatcher = Regex(passwordPattern)

        passwordMatcher.find(it) != null
    } ?: false
}

class LoginInActivity : AppCompatActivity(), OnLoginInFragmentListener {

    private val signInFragment: SignInFragment
    private val signUpSchoolFragment: SignUpSchoolFragment
    private val signUpStudentFragment: SignUpStudentFragment
    private var currentFragment: KindaFragment
    private lateinit var progressBar: ContentLoadingProgressBar
    private lateinit var container: FrameLayout

    init {
        signInFragment = SignInFragment()
        signUpSchoolFragment = SignUpSchoolFragment()
        signUpStudentFragment = SignUpStudentFragment()
        currentFragment = KindaFragment.SIGN_IN
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_in)

        //Инициализируем визуальные компоненты
        progressBar = findViewById(R.id.progressbar)
        container = findViewById(R.id.loginin_fragment_container)

        //Сразу ереходим на фрагмент авторизации
        supportFragmentManager.commit {
            replace(R.id.loginin_fragment_container, signInFragment)
        }
    }

    /**
     * Обработчик перехода с одного фрагмента на другой.
     */
    override fun onFragmentInteraction(fragment: KindaFragment) {
        when(fragment) {
            KindaFragment.SIGN_UP_STUDENT -> {
                //Переходим на фрагмент регистрации студента
                supportFragmentManager.commit {
                    addToBackStack(null)
                    setReorderingAllowed(true)
                    replace(R.id.loginin_fragment_container, signUpStudentFragment)
                }
                currentFragment = KindaFragment.SIGN_UP_STUDENT
            }
            KindaFragment.SIGN_UP_SCHOOL -> {
                //Переходим на фрагмент регистрации школы
                supportFragmentManager.commit {
                    addToBackStack(null)
                    setReorderingAllowed(true)
                    replace(R.id.loginin_fragment_container, signUpSchoolFragment)
                }
                currentFragment = KindaFragment.SIGN_UP_SCHOOL
            }
            KindaFragment.SIGN_IN -> {
                //Возвращаемся на фрагмент авторизации
                if(currentFragment != KindaFragment.SIGN_IN)
                    onBackPressed()
            }
        }
    }

    /**
     * Блокирует пользовательский интерфейс и показывает анимацию загрузки.
     */
    override fun onBlockUI(enable: Boolean) {
        if(enable) progressBar.hide()
        else progressBar.show()
        makeEnableUI(enable, container)
    }

    /**
     * Обработчик окончания регистрации
     */
    override fun onRegistered(who: AccountKind) {
        when(who) {
            //Показываем сообщение для школы
            AccountKind.SCHOOL -> {
                Toast.makeText(this, getText(R.string.notice_school_registration_success), Toast.LENGTH_SHORT).show()
            }
            //Показываем сообщение для студента
            AccountKind.STUDENT -> {
                Toast.makeText(this, getText(R.string.notice_student_registration_success), Toast.LENGTH_SHORT).show()
            }
            //Модератор нерегестрируем
            else -> return
        }
    }

    /**
     * Обработчик окончания авторизации пользователя.
     * Записывает данныые пользователя в файл и возвращает на главную activity.
     */
    override fun onAuthorized(user: Account) {
        //Запись файла прошла успешно - переходим в главную activity
        if(IOFileHelper.updateAccount(this, user)) {
            setResult(RESULT_OK)
            finish()
        }
    }

    /**
     * Обработчик нажатия на кнопку Back на смартфоне.
     */
    override fun onBackPressed() {
        currentFragment = when(currentFragment) {
            KindaFragment.SIGN_UP_SCHOOL -> {
                super.onBackPressed()
                KindaFragment.SIGN_IN
            }
            KindaFragment.SIGN_UP_STUDENT -> {
                super.onBackPressed()
                KindaFragment.SIGN_IN
            }
            else -> return
        }
    }

    public enum class KindaFragment {
        SIGN_IN,
        SIGN_UP_STUDENT,
        SIGN_UP_SCHOOL
    }
}