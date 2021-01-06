package kz.gaudeamus.instudy

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import kz.gaudeamus.instudy.entities.Account
import kz.gaudeamus.instudy.entities.AccountKind

internal const val REQUEST_AUTHORIZATION: Int = 200

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener,
    Toolbar.OnMenuItemClickListener {


    private var currentUser: Account? = null

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

        //Проверяем наличие аккаунта, если нет - переходим на авторизацию
        currentUser = IOFileHelper.anyAccountOrNull(this)
        if(currentUser == null) {
            startActivityForResult(Intent(this, LoginInActivity::class.java), REQUEST_AUTHORIZATION)
        }
        //Обновляем интерфейс под роль пользователя
        else updateUI()

        //Визуальные компоненты
        val navigationMenu: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val appBar: MaterialToolbar = findViewById(R.id.main_appbar)

        //Настраиваем нижнее меню
        navigationMenu.labelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_SELECTED
        navigationMenu.setOnNavigationItemSelectedListener(this)

        //Настраиваем верхнее меню
        appBar.setOnMenuItemClickListener(this)
        //startSupportActionMode(callback) //TODO Вызывает дополнительное меню - долго жмём на карточку
    }

    /**
     * Обработчик нажатия на нижнее меню.
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val menuItem: Menu = findViewById<MaterialToolbar>(R.id.main_appbar).menu

        return when(item.itemId) {
            R.id.main_card_menu -> {
                menuItem.findItem(R.id.appbar_add_card).isVisible = true
                true
            }
            R.id.main_chat_menu -> {
                true
            }
            R.id.main_settings_menu -> {
                menuItem.findItem(R.id.appbar_add_card).isVisible = false
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
                updateUI()
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * Обновляет пользовательский интерфейс.
     */
    private fun updateUI() {
        val navigationMenu: BottomNavigationView = findViewById(R.id.bottom_navigation)

        when(currentUser?.kind) {
            AccountKind.MODERATOR -> {
                //Нижнее меню
                navigationMenu.menu.apply {
                    this.findItem(R.id.main_chat_menu).isVisible = false
                    this.findItem(R.id.main_card_menu).isVisible = false
                }

                //Верхнее меню

            }
            AccountKind.SCHOOL -> {
                navigationMenu.menu.findItem(R.id.main_query_menu).isVisible = false
            }
            AccountKind.STUDENT -> {
                navigationMenu.menu.findItem(R.id.main_query_menu).isVisible = false
            }
        }
    }
}