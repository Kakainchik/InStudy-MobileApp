package kz.gaudeamus.instudy

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class DenyQueryDialogFragment : DialogFragment() {

	private lateinit var commentText: TextInputEditText
	private lateinit var denyView: View

	private var deniableListener: DeniableListener? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		val inflater = requireActivity().layoutInflater
		denyView = inflater.inflate(R.layout.dialog_deny_query, null)

		commentText = denyView.findViewById(R.id.deny_query_comment_text)

		super.onCreate(savedInstanceState)
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return activity?.run {
			val builder = MaterialAlertDialogBuilder(this).apply {
				setTitle(R.string.title_attention)
				setMessage(R.string.alert_sure_deny_query_text)
				setView(denyView)
				setPositiveButton(R.string.bt_ok) { dialog, id ->
					deniableListener?.onDeny(commentText.text.toString().trim())
					dialog.dismiss()
				}
				setNegativeButton(R.string.bt_cancel) { dialog, id ->
					dialog.cancel()
				}
			}

			builder.create()
		} ?: throw IllegalStateException("Activity cannot be null")
	}

	public override fun onAttach(context: Context) {
		this.deniableListener = context as DeniableListener
		super.onAttach(context)
	}

	public interface DeniableListener {
		fun onDeny(comment: String? = null)
	}
}