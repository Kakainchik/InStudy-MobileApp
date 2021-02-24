package kz.gaudeamus.instudy

import android.content.Context
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.children
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.chip.Chip
import kz.gaudeamus.instudy.entities.Account
import kz.gaudeamus.instudy.models.HttpTask
import kz.gaudeamus.instudy.models.StandardHttpViewModel

object UIHelper {
	/**
	 * Обновляет интерфейс. Делает все элементы контейнера доступным или недоступным.
	 * @param enable `true` - делает интерфейс доступным,
	 *               `false` - блокирует
	 */
	public fun makeEnableUI(enable: Boolean, container: ViewGroup) {
		for(c in container.children) {
			c.isEnabled = enable
			if(c is Chip) c.isCloseIconVisible = enable
			//Если следующий View сам представляет из себя контейнер
			if(c is ViewGroup) makeEnableUI(enable, c)
		}
	}

	/**
	 * Показывает всплывающее сообщение о соединении с интернетом либо сервером.
	 */
	public fun toastInternetConnectionError(context: Context, webStatus: HttpTask.WebStatus) {
		val toast: Toast = Toast.makeText(context, null, Toast.LENGTH_SHORT)
		when(webStatus) {
			HttpTask.WebStatus.NONE -> return
			HttpTask.WebStatus.TIMEOUT -> toast.apply { setText(context.getString(R.string.error_http_timeout)) }.show()
			HttpTask.WebStatus.UNABLE_CONNECT -> toast.apply { setText(context.getString(R.string.error_http_connect)) }.show()
			HttpTask.WebStatus.UNAUTHORIZED -> return
		}
	}
}