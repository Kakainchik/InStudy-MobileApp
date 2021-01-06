package kz.gaudeamus.instudy

import android.view.ViewGroup
import androidx.core.view.children
import com.google.android.material.chip.Chip

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
}