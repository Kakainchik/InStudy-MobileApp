package kz.gaudeamus.instudy.models

import android.graphics.Color
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kz.gaudeamus.instudy.R
import kz.gaudeamus.instudy.entities.Card
import kz.gaudeamus.instudy.entities.CardStatus
import java.time.format.DateTimeFormatter

/**
 * Адаптер для списка карточек студента.
 */
class CardAdapter(private val dataSet: List<Card>) :
	RecyclerView.Adapter<CardAdapter.ViewHolder>(), ActionMode.Callback {

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

	public val selectedItems = mutableListOf<Card>()

	public var onCardClickListener: ((Card, Int) -> Unit)? = null
		private set
	public var onCardLongClickListener: (Card.() -> Unit)? = null
		private set
	public var onDeleteItemClickListener: ((List<Card>) -> Unit)? = null
		private set
	public var onCloseActionMode: (() -> Unit)? = null
		private set
	public var multiSelect: Boolean = false

	/**
	 * Устанавливает слушатель для нажатия на карточку в адаптере.
	 */
	public fun setOnItemClickListener(listener: (Card, Int) -> Unit): Unit {
		this.onCardClickListener = listener
	}

	/**
	 * Устанавливает слушатель для долгого нажатия на карточку в адапатере.
	 */
	public fun setOnLongItemClickListener(listener: Card.() -> Unit): Unit {
		this.onCardLongClickListener = listener
	}

	/**
	 * Устанавливает слушатель на удаление карточки из адаптера.
	 */
	public fun setOnDeleteItemClickListener(listener: (List<Card>) -> Unit): Unit {
		this.onDeleteItemClickListener = listener
	}

	/**
	 * Устанавливает слушатель на закрытие экшн меню(при выделении карточки).
	 */
	public fun setOnCloseActionMode(listener: () -> Unit): Unit {
		this.onCloseActionMode = listener
	}

	/**
	 * Обозначает разметку, которая будет использоваться в списке для каждого элемента.
	 */
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.card_list_item, parent, false)
		return ViewHolder(view)
	}

	/**
	 * Инициализирует у каждого элемента списка данные.
	 */
	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		dataSet[position].also { card ->
			holder.titleText.text = card.title
			holder.contentText.text = card.content
			holder.dateText.text = card.created.format(DateTimeFormatter.ISO_LOCAL_DATE)
			holder.cityText.text = card.city
			holder.statusText.apply {
				when(card.status) {
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

			// for every item, check to see if it exists in the selected items array
			if(selectedItems.contains(card)) holder.itemView.alpha = 0.3F
			else holder.itemView.alpha = 1.0F

			holder.itemView.setOnClickListener {
				// if the user is in multi-select mode, add it to the multi select list
				if(multiSelect) selectItem(holder, card)
				else onCardClickListener?.invoke(card, position)
			}

			holder.itemView.setOnLongClickListener {
				// if multiSelect is false, set it to true and select the item
				if(!multiSelect) {
					multiSelect = true

					// Add it to the list containing all the selected images
					selectItem(holder, card)
					onCardLongClickListener?.invoke(card)
				}
				true
			}
		}
	}

	/**
	 * Получает размер списка.
	 */
	override fun getItemCount(): Int = dataSet.size

	private fun selectItem(holder: ViewHolder, card: Card) {
		// If the "selectedItems" list contains the item, remove it and set it's state to normal
		if (selectedItems.contains(card)) {
			selectedItems.remove(card)
			holder.itemView.alpha = 1.0F
			if(selectedItems.isEmpty()) {
				multiSelect = false
				onCloseActionMode?.invoke()
			}
		} else {
			// Else, add it to the list and add a darker shade over the image, letting the user know that it was selected
			selectedItems.add(card)
			holder.itemView.alpha = 0.3F
		}
	}

	override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
		val inflater: MenuInflater? = mode?.menuInflater
		return inflater?.let {
			it.inflate(R.menu.action_bar_menu, menu)
			true
		} ?: false
	}

	override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
		return true
	}

	override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
		return when(item?.itemId) {
			//Нажатие на кнопку удаления карточки(-ек)
			R.id.actionbar_delete_card -> {
				onDeleteItemClickListener?.invoke(selectedItems)
				true
			}
			else -> false
		}
	}

	override fun onDestroyActionMode(mode: ActionMode?) {
		multiSelect = false
		selectedItems.clear()
		notifyDataSetChanged()
	}
}