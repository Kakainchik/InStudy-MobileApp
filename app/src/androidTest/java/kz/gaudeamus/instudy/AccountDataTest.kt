package kz.gaudeamus.instudy

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kz.gaudeamus.instudy.entities.Account
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.charset.Charset

@RunWith(AndroidJUnit4::class)
public class AccountDataTest {

    lateinit var context: Context
    val FILE_NAME = "USER_DATA.json"

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().context
    }

    @Test
    fun hasAccount_isCorrect() {
        //Arrange
        var account: Account?
        val fis = context.openFileInput(FILE_NAME)

        //Act
        val json = fis.use {
            it.readBytes().toString(Charset.defaultCharset())
        }
        account = Json.decodeFromString<Account>(json)

        //Assert
        Assert.assertNotNull(account)
    }
}