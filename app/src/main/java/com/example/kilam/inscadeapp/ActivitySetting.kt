package com.example.kilam.inscadeapp

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.kilam.inscadeapp.R.layout.*
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.dialog_mqtt_host.*

class ActivitySetting : AppCompatActivity(){
    private lateinit var pref : SharedPreferences

    private var mqttHost : String = ""
    private var mqttPort : String = ""
    private var topic : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_setting)

        pref = applicationContext.getSharedPreferences("MQTT", Context.MODE_PRIVATE)
        mqttHost =  pref.getString("mqttHost", "")
        mqttPort =  pref.getString("mqttPort", "")
        topic =  pref.getString("mqttTopic", "")

        tv_host.setText(mqttHost)
        tv_port.setText(mqttPort)
        tv_topic.setText(topic)

        field_host.setOnClickListener {
            edit_("host")
        }
        field_port.setOnClickListener {
            edit_("port")
        }
        field_topic.setOnClickListener {
            edit_("topic")
        }
    }

    private fun edit_(content : String) {
        var dialogs = Dialog(this@ActivitySetting)

        dialogs.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogs.setContentView(dialog_mqtt_host)

        dialogs.window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT)

        dialogs.show()
        val et = dialogs.findViewById(R.id.et_) as EditText
        val btnEdit = dialogs.findViewById(R.id.btn_edit_) as Button

//        val tvtitle = findViewById<TextView>(R.id.tv_title_mqtt)

        if (content.equals("host")) {
//            tvtitle.text = "MQTT HOST"
            et.setText(mqttHost)
        }
        else if (content.equals("port")) {
//            tvtitle.setText("MQTT PORT")
            et.setText(mqttPort)
        }
        else {
            tv_title_mqtt.text = "MQTT TOPIC"
            et.setText(topic)
        }

        btnEdit.setOnClickListener {
            if (content.equals("host")) {
                mqttHost = et.text.toString().trim()
                if (mqttHost.equals("")) {
                    Toast.makeText(this, "There's empty field", Toast.LENGTH_SHORT).show()
                } else {
                    pref.edit().putString("mqttHost", mqttHost).apply()
                    tv_host.setText(mqttHost)
                    Toast.makeText(this, "Successfully changed the Host", Toast.LENGTH_SHORT).show()
                    dialogs.dismiss()
                }
            }
            else if (content.equals("port")){
                mqttPort = et.text.toString().trim()
                if (mqttPort.equals("")) {
                    Toast.makeText(this, "There's empty field", Toast.LENGTH_SHORT).show()
                } else {
                    pref.edit().putString("mqttPort", mqttPort).apply()
                    tv_port.setText(mqttPort)
                    Toast.makeText(this, "Successfully changed the Port", Toast.LENGTH_SHORT).show()
                    dialogs.dismiss()
                }
            }
            else{
                topic = et.text.toString().trim()
                if (topic.equals("")) {
                    Toast.makeText(this, "There's empty field", Toast.LENGTH_SHORT).show()
                } else {
                    pref.edit().putString("mqttTopic", topic).apply()
                    tv_topic.setText(topic)
                    Toast.makeText(this, "Successfully changed the Topic", Toast.LENGTH_SHORT).show()
                    dialogs.dismiss()
                }
            }
        }
    }
}