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
import android.widget.Toast
import com.example.kilam.inscadeapp.R.layout.*
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.dialog_mqtt_host.*

class ActivitySetting : AppCompatActivity(){
    private lateinit var pref : SharedPreferences

    private var mqttHost : String = ""
    private var mqttPort : String = ""
    private var topic1 : String = ""
    private var topic2 : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_setting)

        pref = applicationContext.getSharedPreferences("MQTT", Context.MODE_PRIVATE)
        mqttHost =  pref.getString("mqttHost", "")
        mqttPort =  pref.getString("mqttPort", "")
        topic1 =  pref.getString("mqttTopic1", "")
        topic2 =  pref.getString("mqttTopic2", "")

        tv_host.setText(mqttHost)
        tv_port.setText(mqttPort)
        tv_topic1.setText(topic1)
        tv_topic2.setText(topic2)

        field_host.setOnClickListener {
            edit_("host")
        }
        field_port.setOnClickListener {
            edit_("port")
        }
        field_topic1.setOnClickListener {
            edit_("topic1")
        }
        field_topic2.setOnClickListener {
            edit_("topic2")
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

        if (content.equals("host")) {
            dialogs.tv_title_mqtt.text = "MQTT HOST"
            et.setText(mqttHost)
        }
        else if (content.equals("port")) {
            dialogs.tv_title_mqtt.text = ("MQTT PORT")
            et.setText(mqttPort)
        }
        else if (content.equals("topic1")) {
            dialogs.tv_title_mqtt.text  = "MQTT TOPIC"
            et.setText(topic1)
        }
        else {
            dialogs.tv_title_mqtt.text  = "MQTT TOPIC"
            et.setText(topic2)
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
            else if (content.equals("topic1")){
                topic1 = et.text.toString().trim()
                if (topic1.equals("")) {
                    Toast.makeText(this, "There's empty field", Toast.LENGTH_SHORT).show()
                } else {
                    pref.edit().putString("mqttTopic1", topic1).apply()
                    tv_topic1.setText(topic1)
                    Toast.makeText(this, "Successfully changed the Topic 1", Toast.LENGTH_SHORT).show()
                    dialogs.dismiss()
                }
            }
            else{
                topic2 = et.text.toString().trim()
                if (topic2.equals("")) {
                    Toast.makeText(this, "There's empty field", Toast.LENGTH_SHORT).show()
                } else {
                    pref.edit().putString("mqttTopic2", topic2).apply()
                    tv_topic2.setText(topic2)
                    Toast.makeText(this, "Successfully changed the Topic 2", Toast.LENGTH_SHORT).show()
                    dialogs.dismiss()
                }
            }
        }
    }
}