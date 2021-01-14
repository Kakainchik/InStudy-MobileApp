package kz.gaudeamus.instudy

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.Toolbar
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import kz.gaudeamus.instudy.entities.Account
import kz.gaudeamus.instudy.entities.AccountKind

internal const val REQUEST_AUTHORIZATION: Int = 200

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener,
    Toolbar.OnMenuItemClickListener {

    private var currentUser: Account? = null
    private lateinit var navigationMenu: BottomNavigationView
    private lateinit var appBar: MaterialToolbar
    private lateinit var container: FragmentContainerView
    private lateinit var cardFragment: Fragment

    private val callback = object: ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            menuInflater.inflate(R.menu.action_bar_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return when(item?.itemId) {
                R.id.actionbar_delete_card -> {
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            //NOTHING
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        //Возвращаем стандартную тему после SplashScreen
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Визуальные компоненты
        navigationMenu = findViewById(R.id.bottom_navigation)
        appBar = findViewById(R.id.main_appbar)
        container = findViewById(R.id.main_fragment_container)

        //Проверяем наличие аккаунта, если нет - переходим на авторизацию
        //currentUser = IOFileHelper.anyAccountOrNull(this)
        currentUser = Account(1, "s", "t", "r", AccountKind.STUDENT)
        if(currentUser == null) {
            startActivityForResult(Intent(this, LoginInActivity::class.java), REQUEST_AUTHORIZATION)
        }
        //Обновляем интерфейс под роль пользователя
        else updateUIByAccount()

        //Настраиваем нижнее меню
        navigationMenu.apply {
            this.labelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_SELECTED
            this.setOnNavigationItemSelectedListener(this@MainActivity)
        }

        //Настраиваем верхнее меню
        appBar.setOnMenuItemClickListener(this)
        //startSupportActionMode(callback) //TODO Вызывает дополнительное меню - долго жмём на карточку
    }

    /**
     * Обработчик нажатия на нижнее меню.
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            //Нажата вкладка карточек
            R.id.main_card_menu -> {
                appBar.menu.clear()
                appBar.inflateMenu(R.menu.student_card_appbar_menu)
                supportFragmentManager.commit {
                    addToBackStack("Card")
                    setReorderingAllowed(true)
                    replace(R.id.main_fragment_container, cardFragment)
                }
                true
            }
            R.id.main_chat_menu -> {
                true
            }
            R.id.main_settings_menu -> {
                true
            }
            R.id.main_query_menu -> {
                true
            }
            else -> false
        }
    }

    /**
     * Обработчик нажатия на верхнее меню.
     */
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when(item?.itemId) {
            R.id.appbar_add_card -> { //Создаём новую карточку
                startActivity(Intent(this, CreateCardActivity::class.java))
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
                    //this@MainActivity.onNavigationItemSelected(this.menu[this.selectedItemId])
                }
                //Верхнее меню

            }
            AccountKind.SCHOOL -> {
                navigationMenu.apply {
                    this.inflateMenu(R.menu.school_bottom_navigation_menu)
                    this.selectedItemId = 0
                }
            }
            AccountKind.STUDENT -> {
                this.cardFragment = StudentCardContainerFragment()
                navigationMenu.apply {
                    this.inflateMenu(R.menu.student_bottom_navigation_menu)
                    this@MainActivity.onNavigationItemSelected(this.menu.getItem(0))
                }
            }
        }
    }
}