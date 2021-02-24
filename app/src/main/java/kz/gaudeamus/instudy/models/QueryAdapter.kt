package kz.gaudeamus.instudy.models

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import kz.gaudeamus.instudy.R
import kz.gaudeamus.instudy.entities.SchoolQuery
import java.time.format.DateTimeFormatter

/**
 * Адаптер для списка запросов школ на регистрацию.
 */
class QueryAdapter(private val dataSet: List<SchoolQuery>) : RecyclerView.Adapter<QueryAdapter.ViewHolder>() {

	public class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		val organizationText: MaterialTextView
		val createdText: MaterialTextView
		val verifiedText: MaterialTextView

		init {
			organizationText = view.findViewById(R.id.query_item_organization_text)
			createdText = view.findViewById(R.id.query_item_created_text)
			verifiedText = view.findViewById(R.id.query_item_verified_text)
		}
	}

	public var onQueryClickListener: ((SchoolQuery, Int) -> Unit)? = null
		private set

	/**
	 * Устанавливает слушатель для нажатия на карточку в адаптере.
	 */
	public fun setOnItemClickListener(listener: (SchoolQuery, Int) -> Unit): Unit {
		this.onQueryClickListener = listener
	}

	/**
	 * Обозначает разметку, которая будет использоваться в списке для каждого элемента.
	 */
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.query_list_item, parent, false)
		return ViewHolder(view)
	}

	/**
	 * Инициализирует у каждого элемента списка данные.
	 */
	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		with(dataSet[position]) {
			holder.organizationText.text = this.organization
			holder.createdText.text = this.created.format(DateTimeFormatter.ISO_LOCAL_DATE)

			holder.itemView.setOnClickListener {
				this@QueryAdapter.onQueryClickListener?.invoke(this, position)
			}
		}
	}

	/**
	 * Получает размер списка.
	 */
	override fun getItemCount(): Int = dataSet.size
}