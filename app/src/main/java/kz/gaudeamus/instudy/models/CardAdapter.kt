package kz.gaudeamus.instudy.models

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kz.gaudeamus.instudy.R
import kz.gaudeamus.instudy.entities.Card
import kz.gaudeamus.instudy.entities.CardStatus
import java.time.format.DateTimeFormatter

/**
 * Адаптер для списка карточек студента.
 */
class CardAdapter(private val dataSet: List<Card>) : RecyclerView.Adapter<CardAdapter.ViewHolder>() {

	public class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		val titleText: TextView
		val contentText: TextView
		val dateText: TextView
		val cityText: TextView
		val statusText: TextView

		init {
			titleText = view.findViewById(R.id.card_item_title_text)
			contentText = view.findViewById(R.id.card_item_content_text)
			dateText = view.findViewById(R.id.card_item_date_created_text)
			cityText = view.findViewById(R.id.card_item_city_text)
			statusText = view.findViewById(R.id.card_item_status_text)
		}
	}

	/**
	 * Обозначаем разметку, которая будет использоваться в списке для каждого элемента.
	 */
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.card_list_item, parent, false)
		return ViewHolder(view)
	}

	/**
	 * Инициализируем у каждого элемента списка данные.
	 */
	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		dataSet[position].also {
			holder.titleText.text = it.title
			holder.contentText.text = it.content
			holder.dateText.text = it.created.format(DateTimeFormatter.ISO_LOCAL_DATE)
			holder.cityText.text = it.city
			holder.statusText.apply {
				when(it.status) {
					//Карточка в черновике
					CardStatus.DRAFT -> {
						this.text = rootView.resources.getString(R.string.status_card_draft)
						this.setTextColor(Color.DKGRAY)
					}
					//Карточка активна(на сервере)
					CardStatus.ACTIVE -> {
						this.text = rootView.resources.getString(R.string.status_card_active)
						this.setTextColor(Color.GREEN)
					}
					//Срок действия карточки истёк
					CardStatus.EXPIRED -> {
						this.text = rootView.resources.getString(R.string.status_card_expired)
						this.setTextColor(Color.RED)
					}
				}
			}
		}
	}

	/**
	 * Получает размер списка.
	 */
	override fun getItemCount(): Int = dataSet.size
}