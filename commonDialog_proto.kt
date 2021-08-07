
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.TextView

class CommonDialog(context: Context?, themeResId: Int) : Dialog(context!!, themeResId) {

    class Builder (private val context: Activity) {
        private var title: String? = null
        private var message: String? = null
        private var positiveButtonListener: OnClickPositiveButton? = null

        fun setTitle(title: String): Builder {
            this.title = title
            return this
        }

        fun setMessage(message: String): Builder {
            this.message = message
            return this
        }

        fun setPositiveButton(listener: OnClickPositiveButton?): Builder {
            this.positiveButtonListener = listener
            return this
        }

        fun create(): CommonDialog {
            //init Dialog
            val dialog = CommonDialog(context, R.style.Theme_Panel)
            //init Layout
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val dialogLayoutView = inflater.inflate(R.layout.dialog_common, null)

            val dtt = dialogLayoutView.findViewById<TextView>(R.id.dialog_tip_title)
            val dm = dialogLayoutView.findViewById<TextView>(R.id.tv_message)
            val btY = dialogLayoutView.findViewById<TextView>(R.id.bt_yes)
            dtt.setText(title)
            dm.setText(message)
            btY.apply {
                setOnClickListener {
                    if(positiveButtonListener != null){
                        positiveButtonListener!!.positiveEvent()
                    }else{
                        dialog.dismiss()
                    }
                }
                setText("OK")
            }

            //add view
            dialog.setContentView(dialogLayoutView)

            //Dialog以外の領域クリックした後、Dialog消えない
            dialog.setCanceledOnTouchOutside(false)

            val window = dialog.window
            val size = context.resources.displayMetrics.widthPixels
            window?.let {
                it.attributes.apply {
                    flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
                    dimAmount = 0.6f
                    width = (size * 0.6).toInt()
                    height = (size * 0.3).toInt()
                }
            }

            dialog.show()
            return dialog
        }
    }
    override fun onBackPressed() {}

    interface OnClickPositiveButton {
        fun positiveEvent() {}
    }
}
