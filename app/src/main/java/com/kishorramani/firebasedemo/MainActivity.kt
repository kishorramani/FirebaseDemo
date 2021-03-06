package com.kishorramani.firebasedemo

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.kishorramani.firebasedemo.api.RetrofitInstance
import com.kishorramani.firebasedemo.data.NotificationData
import com.kishorramani.firebasedemo.data.PushNotification
import com.kishorramani.firebasedemo.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val TOPIC = "/topics/myTopic"     //Use for Guest user, Logged in user, Paid user
//const val TOPIC = "kishor"

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseService.sharePreferences = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            FirebaseService.token = it.token
            binding.edtToken.setText(it.token)
        }
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)

        intent.extras?.let {
            for (key in it.keySet()) {
                val value = intent.extras?.get(key)
                Log.e(TAG, "Key: $key Value: $value")
            }
        }

        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
            .addOnCompleteListener { task ->
                var msg = getString(R.string.msg_subscribed)
                if (!task.isSuccessful) {
                    msg = getString(R.string.msg_subscribe_failed)
                }
                Log.d(TAG, msg)
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            }

        binding.btnSend.setOnClickListener {
            val title = binding.edtTitle.text.toString()
            val message = binding.edtMessage.text.toString()
            val recipientToken = binding.edtToken.text.toString()

//            if (title.isNotEmpty() && message.isNotEmpty() && recipientToken.isNotEmpty()) {
            if (title.isNotEmpty() && message.isNotEmpty()) {
                PushNotification(NotificationData(title, message), TOPIC).also {
                    sendNotification(it)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC)
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if (response.isSuccessful) {
                Log.d(TAG, "Response: ${Gson().toJson(response)}")
            } else {
                Log.e(TAG, response.errorBody().toString())
            }
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }
}