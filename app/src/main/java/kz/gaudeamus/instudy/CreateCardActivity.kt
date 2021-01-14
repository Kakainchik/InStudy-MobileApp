package kz.gaudeamus.instudy

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import com.google.android.material.button.MaterialButton
import kz.gaudeamus.instudy.entities.Card
import kz.gaudeamus.instudy.entities.CardStatus
import java.time.LocalDate

public const val CARD_CREATED_CODE: Int = 110

class CreateCardActivity : AppCompatActivity() {

	//Визуальные компоненты
	private lateinit var titleText: EditText
	private lateinit var contentText: EditText
	private lateinit var draftButton: MaterialButton
	private lateinit var createButton: MaterialButton

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_create_card)

		//Инициализируем визуальные компоненты
		titleText = findViewById(R.id.create_card_title_text)
		contentText = findViewById(R.id.create_card_content_text)
		draftButton = findViewById(R.id.create_card_save2draft_button)
		createButton = findViewById(R.id.create_card_create_button)

		//Обработчик нажатия на кнопку сохранения карточки в черновик
		draftButton.setOnClickListener {
			val card = Card(title = titleText.text.toString(),
							 content = contentText.text.toString(),
							 city = "New-York",
							 created = LocalDate.now(),
							 status = CardStatus.DRAFT)

			IOFileHelper.appendCard2Student(this, card)

			setResult(CARD_CREATED_CODE)
			finish()
		}

		//Обработчик нажатия на кнопку создания карточки
		createButton.setOnClickListener {

		}
	}

	override fun onBackPressed() {
		super.onBackPressed()
		//TODO: Всплывающий диалог на сохранение карточки
	}
}