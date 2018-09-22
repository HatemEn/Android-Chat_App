package com.me.hatem.a02_kt_fa.Controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import com.me.hatem.a02_kt_fa.Adapters.MessageAdapter
import com.me.hatem.a02_kt_fa.Model.Channel
import com.me.hatem.a02_kt_fa.Model.Message
import com.me.hatem.a02_kt_fa.R
import com.me.hatem.a02_kt_fa.Services.AuthService
import com.me.hatem.a02_kt_fa.Services.MessageService
import com.me.hatem.a02_kt_fa.Services.UserDataService
import com.me.hatem.a02_kt_fa.Utilities.BROADCAST_USER_DATA_CHANGE
import com.me.hatem.a02_kt_fa.Utilities.SOCKET_URL
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : AppCompatActivity() {

    val socket = IO.socket(SOCKET_URL)
    lateinit var channelAdapter: ArrayAdapter<Channel>
    lateinit var messageAdapter: MessageAdapter
    var selectedChannel: Channel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        //broadcast and socket initialize
        LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver, IntentFilter(BROADCAST_USER_DATA_CHANGE))
        socket.connect()
        socket.on("channelCreated", onNewChannel)
        socket.on("messageCreated", onNewMessage)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        setupAdapters()

        if (App.prefs.isLoggedIn) AuthService.findUserByEmail(this){}

    }

    private val onNewChannel = Emitter.Listener { args ->
      if (App.prefs.isLoggedIn) {
          runOnUiThread {
              val channelName         = args[0] as String
              val channelDescription  = args[1] as String
              val channelId           = args[2] as String

              val channel =  Channel(channelName, channelDescription, channelId)
              MessageService.channels.add(channel)
              channelAdapter.notifyDataSetChanged()
          }
      }
    }

    val onNewMessage = Emitter.Listener { args ->
        if (App.prefs.isLoggedIn) {
            runOnUiThread {
                val channelId   = args[2] as String
                if (channelId == selectedChannel?.id) {
                    val msgBody         = args[0] as String
                    val userName        = args[3] as String
                    val userAvatar      = args[4] as String
                    val userAvatarColor = args[5] as String
                    val id              = args[6] as String
                    val timeStamp       = args[7] as String
                    val newMessage      = Message(msgBody, userName, channelId, userAvatar, userAvatarColor
                            , id, timeStamp)
                    MessageService.messages.add(newMessage)
                    messageAdapter.notifyDataSetChanged()
                    messageListView.smoothScrollToPosition(messageAdapter.itemCount -1)
                }
            }
        }
    }


    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userDataChangeReceiver)
        socket.disconnect()
        super.onDestroy()
    }


    private val userDataChangeReceiver =  object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (App.prefs.isLoggedIn) {
                userNameNavHeader.text  = UserDataService.name
                userEmailNavHeader.text = App.prefs.email
                loginNavHeaderBtn.text  = "Logout"
                val resourceID          = resources.getIdentifier(UserDataService.avatarName,
                        "drawable", packageName)
                userImageNavHeader.setImageResource(resourceID)
                println(UserDataService.avatarColor)
                userImageNavHeader.setBackgroundColor(UserDataService.returnAvatarColor())
                MessageService.getChannels { complete ->
                    if (complete)
                        if (MessageService.channels.count() > 0) {
                            channelAdapter.notifyDataSetChanged()
                            selectedChannel = MessageService.channels[0]
                            updateWithChannel()
                        }
                }

            } else {
                userNameNavHeader.text  = "Login"
                userEmailNavHeader.text = ""
                loginNavHeaderBtn.text  = "Login"
                userImageNavHeader.setImageResource(R.drawable.profiledefault)
                userImageNavHeader.setBackgroundColor(Color.TRANSPARENT)
            }
        }

    }

    fun loginNavHeaderClicked (view: View) {
       if (App.prefs.isLoggedIn) {
           UserDataService.logout(this)
           messageAdapter.notifyDataSetChanged()
           channelAdapter.notifyDataSetChanged()
           mainChannelName.text = "Please Login First"
       } else {
           val  toLoginActivity = Intent(this, LoginActivity::class.java)
           startActivity(toLoginActivity)
       }
    }

    fun addChannelClicked (view: View) {
        if (App.prefs.isLoggedIn) {
            val builder = AlertDialog.Builder(this)
            val dialogView  = layoutInflater.inflate(R.layout.add_channel_dialog, null)
            builder.setView(dialogView)
                    .setPositiveButton("Add") { dialog, which ->
                        val nameText = dialogView.findViewById<EditText>(R.id.channelNameText)
                        val descText = dialogView.findViewById<EditText>(R.id.channelDescText)
                        val channelName = nameText.text.toString()
                        val channelDesc = descText.text.toString()
                        // create channel
                        socket.emit("newChannel", channelName, channelDesc)

                    }
                    .setNegativeButton("Cancel") { dialog, which ->  
                        // cancel the modal
                    }
                    .show()
        }
    }

    fun sendMessageClicked(view: View) {
        if (App.prefs.isLoggedIn && messageTextField.text.isNotEmpty() && selectedChannel != null){
            val userId      = UserDataService.id
            val channelId   = selectedChannel!!.id
            val msgBody     = messageTextField.text.toString()
            val userName    = UserDataService.name
            val avatar      = UserDataService.avatarName
            val avatarColor = UserDataService.avatarColor
            socket.emit("newMessage", msgBody, userId, channelId,
                    userName, avatar, avatarColor)
            messageTextField.text.clear()
            hideKeyboard()
        }

    }


    fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputManager.isAcceptingText)
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
    }

    fun setupAdapters() {
        channelAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, MessageService.channels)
        channel_list.adapter = channelAdapter
        channel_list.setOnItemClickListener { parent, view, position, id ->
            selectedChannel = MessageService.channels[position]
            updateWithChannel()
        }
        messageAdapter = MessageAdapter(this, MessageService.messages)
        messageListView.adapter = messageAdapter
        val layoutManger = LinearLayoutManager(this)
        messageListView.layoutManager = layoutManger
    }

    fun updateWithChannel() {
        mainChannelName.text = "#${selectedChannel?.name}"
        if (selectedChannel != null) {
            MessageService.getMessages(selectedChannel!!.id) { complete ->
                if (complete) {
                    messageAdapter.notifyDataSetChanged()
                    if (messageAdapter.itemCount > 0) messageListView
                            .smoothScrollToPosition(messageAdapter.itemCount -1)
                }
            }
        }
    }

}
