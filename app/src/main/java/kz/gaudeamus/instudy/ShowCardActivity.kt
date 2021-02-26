package kz.gaudeamus.instudy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class ShowCardActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_show_card)
	}

	companion object {
		public const val NAME_EXTRA = "CARD"
	}
}