package kz.gaudeamus.instudy

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import kz.gaudeamus.instudy.entities.Account
import kz.gaudeamus.instudy.entities.AccountKind

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener, OnLogoutListener {
    /**
     * Обработчик события на авторизацию пользователя из [LoginInActivity]
     */
    private val loginCallback: ActivityResultLauncher<Nothing> = registerForActivityResult(LoginActivityContract()) {
        if(it) {
            currentAccount = IOFileHelper.anyAccountOrNull(this)
            updateUIByAccount()
        }
    }

    private var currentAccount: Account? = null
    private lateinit var navigationMenu: BottomNavigationView
    private lateinit var cardFragment: Fragment
    private lateinit var settingsFragment: Fragment
    private lateinit var queryFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        //Возвращаем стандартную тему после SplashScreen
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Визуальные компоненты
        navigationMenu = findViewById(R.id.bottom_navigation)

        //Настраиваем нижнее меню
        navigationMenu.apply {
            this.labelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_SELECTED
            this.setOnNavigationItemSelectedListener(this@MainActivity)
        }

        //Проверяем наличие аккаунта, если нет - переходим на авторизацию
        currentAccount = IOFileHelper.anyAccountOrNull(this)
        if(currentAccount == null) loginCallback.launch(null)
        //Обновляем интерфейс под роль пользователя
        else updateUIByAccount()
    }

    /**
     * Обработчик нажатия на нижнее меню.
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            //Нажата вкладка карточек
            R.id.main_card_menu -> {
                supportFragmentManager.commit(true) {
                    addToBackStack("StudentCardContainerFragment")
                    setReorderingAllowed(true)
                    replace(R.id.main_fragment_container, cardFragment)
                }
                true
            }
            //Нажата вкладка чата
            R.id.main_chat_menu -> {
                true
            }
            //Нажата вкладка настроек
            R.id.main_settings_menu -> {
                supportFragmentManager.commit(true) {
                    addToBackStack("SettingsFragment")
                    setReorderingAllowed(true)
                    replace(R.id.main_fragment_container, settingsFragment)
                }
                true
            }
            R.id.main_query_menu -> {
                supportFragmentManager.commit(true) {
                    addToBackStack("QueryFragment")
                    setReorderingAllowed(true)
                    replace(R.id.main_fragment_container, queryFragment)
                }
                true
            }
            else -> false
        }
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        //NOTHING
    }

    override fun onLogout() {
        if(IOFileHelper.deleteAccount(this)) {
            when(currentAccount?.kind) {
                //FIXME Не обновляет нижнее меню при перезаходе на другой аккаунт
                AccountKind.STUDENT -> cardFragment.onDestroy()
                AccountKind.SCHOOL -> TODO()
                AccountKind.MODERATOR -> queryFragment.onDestroy()
            }
            settingsFragment.onDestroy()
            loginCallback.launch(null)
        }
        else Log.w("LOGOUT", "Unexpected error. Couldn't delete account file.")
    }

    /**
     * Обновляет пользовательский интерфейс на основе имеющегося аккаунта.
     */
    private fun updateUIByAccount() {
        navigationMenu.menu.clear()
        when(currentAccount?.kind) {
            AccountKind.MODERATOR -> {
                this.settingsFragment = SettingsFragment()
                this.queryFragment = ModeratorQueryContainerFragment()
                //Нижнее меню
                navigationMenu.apply {
                    this.inflateMenu(R.menu.moderator_bottom_navigation_menu)
                    this@MainActivity.onNavigationItemSelected(this.menu.getItem(0))
                }
            }
            AccountKind.SCHOOL -> {
                //Нижнее меню
                navigationMenu.apply {
                    this.inflateMenu(R.menu.school_bottom_navigation_menu)
                    this@MainActivity.onNavigationItemSelected(this.menu.getItem(0))
                }
            }
            AccountKind.STUDENT -> {
                this.settingsFragment = SettingsFragment()
                this.cardFragment = StudentCardContainerFragment()
                //Нижнее меню
                navigationMenu.apply {
                    this.inflateMenu(R.menu.student_bottom_navigation_menu)
                    this@MainActivity.onNavigationItemSelected(this.menu.getItem(0))
                }
            }
        }
    }

    /**
     * Класс для перехода на страницу авторизации и получения итогового результата от неё.
     */
    private class LoginActivityContract : ActivityResultContract<Nothing, Boolean>() {
        override fun createIntent(context: Context, input: Nothing?): Intent {
            return Intent(context, LoginInActivity::class.java)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
            return resultCode == Activity.RESULT_OK
        }
    }
}