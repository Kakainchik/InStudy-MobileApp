package kz.gaudeamus.instudy

import kz.gaudeamus.instudy.entities.Account
import kz.gaudeamus.instudy.entities.AccountKind

interface OnLoginInFragmentListener {

    fun onFragmentInteraction(fragment: LoginInActivity.KindaFragment)
    fun onBlockUI(enable: Boolean)
    fun onRegistered(who: AccountKind)
    fun onAuthorized(user: Account)
}