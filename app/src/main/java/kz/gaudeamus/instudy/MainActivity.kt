package kz.gaudeamus.instudy

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import com.google.android.material.internal.NavigationMenu
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.lang.Exception
import java.nio.charset.Charset

internal const val ACCOUNT_FILE_NAME = "USER_DATA.json"

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private var currentUser: Account? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        //Возвращаем стандартную тему после SplashScreen
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        hasAnyAccount()
        updateUI()
        /*val preferences = getSharedPreferences(PREFS_ACCOUNT, MODE_PRIVATE)
        if(!preferences.contains(ACCOUNT_ID_KEY)) {
            //TODO: Переход на страницу аутентификации
            //
        }*/
        startActivity(Intent(this, LoginInActivity::class.java))
        //Визуальные компоненты
        val navigationMenu: BottomNavigationView = findViewById(R.id.bottom_navigation)

        //Настраиваем нижнее меню
        navigationMenu.labelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_SELECTED
        navigationMenu.setOnNavigationItemSelectedListener(this)
    }

    //Обработчик нажатия на нижнее меню
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.main_card_menu -> {
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

    //Обновляем пользовательский интерфейс
    private fun updateUI() {
        val navigationMenu: BottomNavigationView = findViewById(R.id.bottom_navigation)

        when(currentUser?.kind) {
            AccountKind.MODERATOR -> {
                navigationMenu.menu.apply {
                    this.findItem(R.id.main_chat_menu).isVisible = false
                    this.findItem(R.id.main_card_menu).isVisible = false
                }
            }
            AccountKind.SCHOOL -> {
                navigationMenu.menu.findItem(R.id.main_query_menu).isVisible = false
            }
            AccountKind.STUDENT -> {
                navigationMenu.menu.findItem(R.id.main_query_menu).isVisible = false
            }
        }
    }

    private fun hasAnyAccount(): Boolean {

        val file = File(this.dataDir, ACCOUNT_FILE_NAME)
        val fin = openFileInput(ACCOUNT_FILE_NAME)

        //Если нет файла - сразу создаём и возвращаем false
        return if(!file.exists()) !file.createNewFile()
        //Если есть, но делаем проверку на целостность
        else {
            val json = fin.use {
                it.readBytes().toString(Charset.defaultCharset())
            }
            currentUser = try {
                Json.decodeFromString<Account>(json)
            } catch(ex: Exception) {
                return false
            }
            true
        }
    }
}