package kz.gaudeamus.instudy

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import kz.gaudeamus.instudy.entities.Account
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.nio.charset.Charset

object IOFileHelper {

	private const val TAG = "IOFileHelper"
	private const val ACCOUNT_FILE_NAME = "USER_DATA.json"

	/**
	 *	Возвращает текущий активный аккаунт в приложении либо null, если такового не имеется.
	 */
	public fun anyAccountOrNull(context: Context): Account? {
		val file = File(context.dataDir, ACCOUNT_FILE_NAME)

		return try {
			val fin = context.openFileInput(ACCOUNT_FILE_NAME)
			//Читаем данные с файла
			val json = fin.use {
				it.readBytes().toString(Charset.defaultCharset())
			}
			Json.decodeFromString<Account>(json)
		} catch(ex: Exception) {
			/*Если конвертация с JSON в Account не удалась или не удаётся прочесть файл
			Создаём пустой файл и возвращаем null*/
			file.createNewFile()
			null
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
	 * Удаляет файл с аккаунтом пользователя.
	 */
	public fun deleteAccount(context: Context): Boolean {
		val file = File(context.dataDir, ACCOUNT_FILE_NAME)

		return file.delete()
	}

	public fun saveProps(context: Context, schoolId: Long, propsName: String, propsData: ByteArray) {
		val file = File("${context.cacheDir.path}/$schoolId", propsName)
		if(!file.exists()) {
			file.parentFile?.mkdir()
			file.createNewFile()
		}
		try {
			file.writeBytes(propsData)
		} catch(ex: IOException) {
			Log.e(TAG, "Could not save props.", ex)
		}
	}

	fun takePropsFile(context: Context, schoolId: Long, propsName: String): File {
		return try {
			File("${context.cacheDir.path}/$schoolId", propsName)
		} catch(ex: Exception) {
			throw IOException(ex)
		}
	}
}