package kz.gaudeamus.instudy

import android.content.Context
import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import kz.gaudeamus.instudy.entities.Account
import kz.gaudeamus.instudy.entities.Card
import java.io.File
import java.lang.Exception
import java.nio.charset.Charset

object IOFileHelper {

	internal const val ACCOUNT_FILE_NAME = "USER_DATA.json"
	internal const val CARDS_FILE_NAME = "CARDS_DATA.json"

	/**
	 *	Возвращает текущий активный аккаунт в приложении либо null, если такового не имеется.
	 */
	public fun anyAccountOrNull(context: Context): Account? {

		val file = File(context.dataDir, ACCOUNT_FILE_NAME)

		//Если нет файла - сразу создаём и возвращаем null
		return if(!file.exists()) {
			file.createNewFile()
			null
		}
		//Если есть, но делаем проверку на целостность
		else {
			val account = try {
				val fin = context.openFileInput(ACCOUNT_FILE_NAME)
				val json = fin.use {
					it.readBytes().toString(Charset.defaultCharset())
				}
				Json.decodeFromString<Account>(json)
			} catch(ex: Exception) {
				//Если конвертация с JSON в Account не удалась
				return null
			}
			account
		}
	}

	/**
	 * Обновляет все данные активного аккаунта в приложении. `True`, если успешно.
	 */
	public fun updateAccount(context: Context, account: Account): Boolean {
		val file = File(context.dataDir, ACCOUNT_FILE_NAME)

		return try {
			context.openFileOutput(file.name, Context.MODE_PRIVATE).use {
				it.write(Json.encodeToString(account).toByteArray())
			}
			true
		} catch(ex: Exception) {
			false
		}
	}

	/**
	 * Добавляет новую карточку в список всех других карт студента.
	 */
	public fun appendCard2Student(context: Context, card: Card) : Boolean {
		val file = File(context.dataDir, CARDS_FILE_NAME)

		try {
			if(!file.exists()) {
				file.createNewFile()
				Log.i("APPEND CARD", "Creating new empty file")

				context.openFileOutput(file.name, Context.MODE_PRIVATE).use {
					it.write("""[]""".toByteArray())
				}
			}

			var cards: Array<Card> = arrayOf()
			val json = context.openFileInput(file.name).use {
				val bytes = it.readBytes()
				bytes.toString(Charset.defaultCharset())
			}

			cards = Json.decodeFromString(json)
			cards = cards.plus(card)

			context.openFileOutput(file.name, Context.MODE_PRIVATE).use {
				it.write(Json.encodeToString(cards).toByteArray())
			}
			return true
		} catch(ex: Exception) {
			ex.message?.let { Log.e("APPEND CARD", it) }
			return false
		}
	}
}