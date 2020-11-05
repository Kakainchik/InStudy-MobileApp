package kz.gaudeamus.instudy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class LoginInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        //Возвращаем стандартную тему после SplashScreen
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_in)
    }
}