package kz.gaudeamus.instudy

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import kz.gaudeamus.instudy.entities.Account
import kz.gaudeamus.instudy.entities.AccountKind

internal const val REQUEST_AUTHORIZATION: Int = 200
internal const val DATABASE_NAME = "INSTUDY-DATABASE"

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private var currentUser: Account? = null
    private lateinit var navigationMenu: BottomNavigationView
    private lateinit var container: FragmentContainerView
    private lateinit var cardFragment: Fragment
    private lateinit var settingsFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        //Возвращаем стандартную тему после SplashScreen
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Визуальные компоненты
        navigationMenu = findViewById(R.id.bottom_navigation)
        container = findViewById(R.id.main_fragment_container)

        //Настраиваем нижнее меню
        navigationMenu.apply {
            this.labelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_SELECTED
            this.setOnNavigationItemSelectedListener(this@MainActivity)
        }

        //Проверяем наличие аккаунта, если нет - переходим на авторизацию
        currentUser = IOFileHelper.anyAccountOrNull(this)
        if(currentUser == null) {
            startActivityForResult(Intent(this, LoginInActivity::class.java), REQUEST_AUTHORIZATION)
        }
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
                    addToBackStack("Card")
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
                    addToBackStack("Settings")
                    setReorderingAllowed(true)
                    replace(R.id.main_fragment_container, settingsFragment)
                }
                true
            }
            R.id.main_query_menu -> {
                true
            }
            else -> false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //Ответы от авторизации пока что только
        when(resultCode) {
            RESULT_OK -> {
                currentUser = IOFileHelper.anyAccountOrNull(this)
                updateUIByAccount()
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        //NOTHING
    }

    /**
     * Обновляет пользовательский интерфейс на основе имеющегося аккаунта.
     */
    private fun updateUIByAccount() {
        when(currentUser?.kind) {
            AccountKind.MODERATOR -> {
                //Нижнее меню
                navigationMenu.apply {
                    this.inflateMenu(R.menu.moderator_bottom_navigation_menu)
                    this.selectedItemId = 0
                }
            }
            AccountKind.SCHOOL -> {
                navigationMenu.apply {
                    this.inflateMenu(R.menu.school_bottom_navigation_menu)
                    this.selectedItemId = 0
                }
            }
            AccountKind.STUDENT -> {
                this.settingsFragment = SettingsFragment()
                this.cardFragment = StudentCardContainerFragment()
                navigationMenu.apply {
                    this.inflateMenu(R.menu.student_bottom_navigation_menu)
                    this@MainActivity.onNavigationItemSelected(this.menu.getItem(0))
                }
            }
        }
    }
}