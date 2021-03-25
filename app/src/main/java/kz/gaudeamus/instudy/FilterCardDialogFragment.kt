package kz.gaudeamus.instudy

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kz.gaudeamus.instudy.entities.CardFilter
import kz.gaudeamus.instudy.models.CardSchoolViewModel

class FilterCardDialogFragment(var filter: CardFilter = CardFilter(null, null, null, null)) : DialogFragment() {
	//Визуальные компоненты
	private lateinit var titleText: TextInputEditText
	private lateinit var cityText: TextInputEditText
	private lateinit var facultyText: TextInputEditText
	private lateinit var specialityText: TextInputEditText
	private lateinit var titleLayout: TextInputLayout
	private lateinit var cityLayout: TextInputLayout
	private lateinit var facultyLayout: TextInputLayout
	private lateinit var specialityLayout: TextInputLayout
	private lateinit var titleCheckBox: MaterialCheckBox
	private lateinit var cityCheckBox: MaterialCheckBox
	private lateinit var facultyCheckBox: MaterialCheckBox
	private lateinit var specialityCheckBox: MaterialCheckBox
	private lateinit var countValueText: TextView
	private lateinit var countSeekBar: SeekBar
	private lateinit var filterView: View
	private lateinit var settings: SharedPreferences

	private var countCard: Int = 50

	private val viewModel: CardSchoolViewModel by activityViewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		val inflater = requireActivity().layoutInflater
		filterView = inflater.inflate(R.layout.dialog_filter_card, null)

		//Инициализируем визуальные компоненты
		titleText = filterView.findViewById(R.id.filter_card_title_text)
		cityText = filterView.findViewById(R.id.filter_card_city_text)
		facultyText = filterView.findViewById(R.id.filter_card_faculty_text)
		specialityText = filterView.findViewById(R.id.filter_card_speciality_text)
		titleLayout = filterView.findViewById(R.id.filter_card_title_layout)
		cityLayout = filterView.findViewById(R.id.filter_card_city_layout)
		facultyLayout = filterView.findViewById(R.id.filter_card_faculty_layout)
		specialityLayout = filterView.findViewById(R.id.filter_card_speciality_layout)
		titleCheckBox = filterView.findViewById(R.id.checkbox_title)
		cityCheckBox = filterView.findViewById(R.id.checkbox_city)
		facultyCheckBox = filterView.findViewById(R.id.checkbox_faculty)
		specialityCheckBox = filterView.findViewById(R.id.checkbox_speciality)
		countValueText = filterView.findViewById(R.id.filter_card_count_seekbar_value)
		countSeekBar = filterView.findViewById(R.id.filter_card_count_seekbar)

		countValueText.text = getString(R.string.title_seekbar_count_card, countSeekBar.progress)

		titleCheckBox.setOnCheckedChangeListener { _, isChecked ->
			titleLayout.isEnabled = isChecked
		}

		cityCheckBox.setOnCheckedChangeListener { _, isChecked ->
			cityLayout.isEnabled = isChecked
		}

		facultyCheckBox.setOnCheckedChangeListener { _, isChecked ->
			facultyLayout.isEnabled = isChecked
		}

		specialityCheckBox.setOnCheckedChangeListener { _, isChecked ->
			specialityLayout.isEnabled = isChecked
		}

		countSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
			override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
				countValueText.text = getString(R.string.title_seekbar_count_card, progress)
			}

			override fun onStartTrackingTouch(seekBar: SeekBar?) {
				//NOTHING
			}

			override fun onStopTrackingTouch(seekBar: SeekBar?) {
				countCard = seekBar!!.progress
			}
		})

		filter.title?.let {
			titleCheckBox.isChecked = true
			titleText.setText(it)
		}
		filter.city?.let {
			cityCheckBox.isChecked = true
			cityText.setText(it)
		}
		filter.faculty?.let {
			facultyCheckBox.isChecked = true
			facultyText.setText(it)
		}
		filter.speciality?.let {
			specialityCheckBox.isChecked = true
			specialityText.setText(it)
		}
		filter.count.let {
			countCard = it
			countSeekBar.progress = it
		}

		super.onCreate(savedInstanceState)
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return activity?.run {
			settings = getSharedPreferences("PERSONAL_DATA_KEY", Context.MODE_PRIVATE)
			if(settings.contains("DEFAULT_CITY") && filter.city == null) {
				cityCheckBox.isChecked = true
				cityText.setText(settings.getString("DEFAULT_CITY", null))
			}

			val builder = MaterialAlertDialogBuilder(this).apply {
				setTitle(R.string.title_filter_card_by)
				setView(filterView)
				setPositiveButton(R.string.bt_ok) { dialog, id ->
					val title = titleText.text?.trim().toString().takeIf { titleCheckBox.isChecked }
					val city = cityText.text?.trim().toString().takeIf { cityCheckBox.isChecked }
					val faculty = facultyText.text?.trim().toString().takeIf { facultyCheckBox.isChecked }
					val speciality = specialityText.text?.trim().toString().takeIf { specialityCheckBox.isChecked }
					filter = CardFilter(city = city,
										faculty = faculty,
										speciality = speciality,
										title = title,
										count = countCard)
					viewModel.filterLiveData.postValue(filter)
					dialog.dismiss()
				}
				setNegativeButton(R.string.bt_cancel) { dialog, id ->
					dialog.cancel()
				}
			}

			builder.create()
		} ?: throw IllegalStateException("Activity cannot be null")
	}
}