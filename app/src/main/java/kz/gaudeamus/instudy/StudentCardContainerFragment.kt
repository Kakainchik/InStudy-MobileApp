package kz.gaudeamus.instudy

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kz.gaudeamus.instudy.entities.Card
import kz.gaudeamus.instudy.models.CardAdapter

class StudentCardContainerFragment : Fragment() {

	private val cards = mutableListOf<Card>()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val view = inflater.inflate(R.layout.fragment_student_card_container, container, false)

		//Визуальные компоненты
		val cardList: RecyclerView = view.findViewById(R.id.card_list)

		cardList.adapter = CardAdapter(cards)

		return view
	}
}