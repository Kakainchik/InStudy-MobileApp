package kz.gaudeamus.instudy

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.appbar.MaterialToolbar

class SettingsFragment : Fragment() {

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View? {
		val view = inflater.inflate(R.layout.fragment_settings, container, false)

		val appBar: MaterialToolbar = view.findViewById(R.id.main_appbar)

		return view
	}
}