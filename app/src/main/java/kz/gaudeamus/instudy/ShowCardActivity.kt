package kz.gaudeamus.instudy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textview.MaterialTextView
import kz.gaudeamus.instudy.entities.FilteredCard
import java.time.format.DateTimeFormatter

class ShowCardActivity : AppCompatActivity() {
	//Визуальные компоненты
	private lateinit var titleText: MaterialTextView
	private lateinit var commentText: MaterialTextView
	private lateinit var cityText: MaterialTextView
	private lateinit var specialityText: MaterialTextView
	private lateinit var facultyText: MaterialTextView
	private lateinit var fullNameText: MaterialTextView
	private lateinit var phoneText: MaterialTextView
	private lateinit var emailText: MaterialTextView
	private lateinit var appBar: MaterialToolbar

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_show_card)

		//Инициализируем визуальные компоненты
		titleText = findViewById(R.id.show_card_title_title)
		commentText = findViewById(R.id.show_card_comment_title)
		cityText = findViewById(R.id.show_card_city_title)
		specialityText = findViewById(R.id.show_card_speciality_title)
		facultyText = findViewById(R.id.show_card_faculty_title)
		fullNameText = findViewById(R.id.show_card_fullname_title)
		phoneText = findViewById(R.id.show_card_phone_title)
		emailText = findViewById(R.id.show_card_email_title)
		appBar = findViewById(R.id.show_card_appbar)

		//В любом случае должна прийти не null карта
		val bundle: FilteredCard = intent.getSerializableExtra(NAME_EXTRA) as FilteredCard
		bundle.run {
			titleText.text = card.title
			if(card.content.isNullOrBlank()) {
				commentText.visibility = View.GONE
				findViewById<View>(R.id.comment_divider).visibility = View.GONE
			} else commentText.text = card.content
			cityText.text = getString(R.string.title_showcard_city, card.city)
			specialityText.text = getString(R.string.title_showcard_speciality, card.speciality)
			facultyText.text = getString(R.string.title_showcard_faculty, card.faculty)
			fullNameText.text = getString(R.string.title_showcard_fullname, student.name, student.surname ?: "")
			phoneText.text = getString(R.string.title_showcard_phone, student.phone)
			emailText.text = getString(R.string.title_showcard_email, student.email)
			appBar.title = student.name
			appBar.subtitle = getString(R.string.title_card_created, card.created.format(DateTimeFormatter.ISO_LOCAL_DATE))
		}
	}

	companion object {
		public const val NAME_EXTRA = "CARD"
	}
}