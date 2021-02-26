package kz.gaudeamus.instudy.models

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kz.gaudeamus.instudy.R
import kz.gaudeamus.instudy.entities.FilteredCard
import java.time.format.DateTimeFormatter

class CardSchoolAdapter(private val dataSet: List<FilteredCard>) : RecyclerView.Adapter<CardSchoolAdapter.ViewHolder>() {
	public class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		val titleText: TextView
		val contentText: TextView
		val dateText: TextView
		val cityText: TextView

		init {
			titleText = view.findViewById(R.id.card_item_title_text)
			contentText = view.findViewById(R.id.card_item_content_text)
			dateText = view.findViewById(R.id.card_item_date_created_text)
			cityText = view.findViewById(R.id.card_item_city_text)
		}
	}

	public var onCardClickListener: ((FilteredCard, Int) -> Unit)? = null
		private set

	/**
	 * Устанавливает слушатель для нажатия на карточку в адаптере.
	 */
	public fun setOnItemClickListener(listener: (FilteredCard, Int) -> Unit): Unit {
		this.onCardClickListener = listener
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardSchoolAdapter.ViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.school_card_list_item, parent, false)
		return CardSchoolAdapter.ViewHolder(view)
	}

	override fun onBindViewHolder(holder: CardSchoolAdapter.ViewHolder, position: Int) {
		with(dataSet[position]) {
			holder.titleText.text = card.title
			holder.contentText.text = card.content
			holder.dateText.text = card.created.format(DateTimeFormatter.ISO_LOCAL_DATE)
			holder.cityText.text = card.city

			holder.itemView.setOnClickListener {
				onCardClickListener?.invoke(this, position)
			}
		}
	}

	override fun getItemCount(): Int = dataSet.size
}