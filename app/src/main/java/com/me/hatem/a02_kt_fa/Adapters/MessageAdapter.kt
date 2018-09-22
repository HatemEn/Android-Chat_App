package com.me.hatem.a02_kt_fa.Adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.me.hatem.a02_kt_fa.Model.Message
import com.me.hatem.a02_kt_fa.R
import com.me.hatem.a02_kt_fa.Services.UserDataService
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(val context: Context, val messages: ArrayList<Message>) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.message_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return messages.count()
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.messageBind(context, messages[position])
    }

    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView){
        val messageUserImage = itemView?.findViewById<ImageView>(R.id.messageUserImage)
        val messageUserName  = itemView?.findViewById<TextView>(R.id.messageUserName)
        val messageTimeStamp = itemView?.findViewById<TextView>(R.id.messageTimeStamp)
        val messageBody      = itemView?.findViewById<TextView>(R.id.messageBody)

        fun messageBind(context: Context, message: Message) {
            val resourceId = context.resources.getIdentifier(message.userAvatar, "drawable"
                    , context.packageName)
            messageUserImage?.setImageResource(resourceId)
            messageUserImage?.setBackgroundColor(UserDataService.returnAvatarColor(message.userAvatarColor))
            messageUserName?.text   = message.userName
            messageTimeStamp?.text  = returnDateString(message.timeStamp)
            messageBody?.text       = message.message

        }

        fun returnDateString(iosString: String) : String {
            // Convert "2018-08-07T18:04:30.121Z" to "Tue, 9:04 PM"
            val iosFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            iosFormatter.timeZone = TimeZone.getTimeZone("UTC")
            var convertedDate = Date()
            try {
                convertedDate = iosFormatter.parse(iosString)
            } catch (e: ParseException) {
                Log.d("PARSE", "Cannot parse date")
            }

            val outerDateFormat = SimpleDateFormat("E, h:mm a", Locale.getDefault())
            return outerDateFormat.format(convertedDate)
        }
    }
}