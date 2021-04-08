package kz.gaudeamus.instudy.models

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kz.gaudeamus.instudy.IOFileHelper
import kz.gaudeamus.instudy.SingleLiveEvent
import kz.gaudeamus.instudy.entities.Account
import kz.gaudeamus.instudy.entities.RefreshTokenResponse

abstract class StandardHttpViewModel(application: Application) : AndroidViewModel(application) {
	protected abstract val repository: KtorRepository
	public val refreshLiveData = SingleLiveEvent<RefreshTokenResponse>()

	/**
	 * Обновляет активные токен через RefreshToken.
	 */
	public open fun refreshToken(refreshToken: String) {
		viewModelScope.launch {
			val result: HttpTask<RefreshTokenResponse> = repository.makeRequest {
				repository.makeRefreshTokenRequest(refreshToken)
			}

			result.data?.let { refreshLiveData.postValue(it) }
		}
	}

	/**
	 * Осуществляет выполнение метода [action] через получения нового активного токена.
	 */
	public inline fun throughRefreshToken(context: Context, account: Account, crossinline action: (account: Account) -> Unit) {
		refreshLiveData.observe(context as LifecycleOwner) {
			val newAccount = account.updateToken(it.token, it.refreshToken!!)
			if(IOFileHelper.updateAccount(context, newAccount)) action.invoke(newAccount)
		}

		//Пробуем обновить наш токен
		refreshToken(account.refreshToken)
	}
}