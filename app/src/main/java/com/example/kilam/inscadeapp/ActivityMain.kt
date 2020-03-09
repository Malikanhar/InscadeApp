package com.example.kilam.inscadeapp

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.kilam.inscadeapp.R.layout.fragment_dashboard
import com.google.firebase.auth.FirebaseAuth
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.GridLabelRenderer
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.fragment_dashboard.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.IMqttActionListener

class ActivityMain : AppCompatActivity(){
    private var list_patient = ArrayList<Patient>()
    private var list_nama = ArrayList<String>()

    private var isConnected : Boolean = false
    private var i = 0
    private var mqttHost = ""
    private var mqttPort = ""
    private var topic1 = ""
    private var topic2 = ""

    private lateinit var adapterResult: AdapterResult
    private lateinit var mAdapter : ArrayAdapter<String>
    private lateinit var client : MqttAndroidClient
    private lateinit var mChart : GraphView
    private lateinit var mSeries : LineGraphSeries<DataPoint>

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(fragment_dashboard)

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        initView(true)

        btn_subs.setOnClickListener { subscribeTopic(true, 0) }
        iv_reset.setOnClickListener { initView(false) }
        tx_reconnect.setOnClickListener{ connectMQTT() }

        list_patient = generatePatient()
        list_nama = getList_nama(list_patient)

        list_nama.add(0, "Select Patient")
        mAdapter = ArrayAdapter(this, R.layout.item_spinner, list_nama)
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_patient.adapter = mAdapter
        spinner_patient.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(position > 0){
                    btn_subs.visibility = View.VISIBLE
                    cv_patient.visibility = View.VISIBLE
                    tv_patient_name.text = list_patient[position-1].name
                    tv_patient_age.text = list_patient[position-1].age + " years old"
                    tv_patient_gender.text = list_patient[position-1].gender
                }else{
                    cv_patient.visibility = View.GONE
                    btn_subs.visibility = View.GONE
                }
            }
        }
        adapterResult = AdapterResult()
        rv_result.apply {
            layoutManager = LinearLayoutManager(this@ActivityMain)
            adapter = adapterResult
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.setting ->{
                startActivity(Intent(this@ActivityMain, ActivitySetting::class.java))
                return true
            }
            R.id.logout ->{
                close()
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this@ActivityMain, ActivityLogin::class.java))
                finish()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun connectMQTT(){
        val clientId = MqttClient.generateClientId()
        mqttHost =  applicationContext.getSharedPreferences("MQTT", Context.MODE_PRIVATE).getString("mqttHost", "")
        mqttPort =  applicationContext.getSharedPreferences("MQTT", Context.MODE_PRIVATE).getString("mqttPort", "")
        client = MqttAndroidClient(this.applicationContext, "tcp://${mqttHost}:${mqttPort}", clientId)
        try {
            val token = client.connect()
            token.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    // We are connected
                    Toast.makeText(this@ActivityMain, "Device is connected", Toast.LENGTH_SHORT).show()
                    setConnection(true)
                    spinner_patient.visibility = View.VISIBLE
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Toast.makeText(this@ActivityMain, "Device is not connected", Toast.LENGTH_SHORT).show()
                    setConnection(false)
                    spinner_patient.visibility = View.GONE
                }
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }
        client.setCallback(object : MqttCallback{
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                if(topic.equals(this@ActivityMain.topic1)) {
//                    Log.d("DATA", String(message!!.payload))
                    if(message!!.payload != null) {
                        var strs = String(message!!.payload).split(",").toTypedArray()
                        strs.forEach {
                            Log.d("DATAA", it)
                            mSeries.appendData(DataPoint(i.toDouble(), it.toDouble()), true, 8000)
                            i += 1
                        }
                    }
                }else{
                    if (message!!.payload != null)
                        cv_result.visibility = View.VISIBLE
                    adapterResult.addItem(Result(String(message!!.payload)))
                    rv_result.smoothScrollToPosition(adapterResult.itemCount - 1)
                }
            }

            override fun connectionLost(cause: Throwable?) {
                Toast.makeText(this@ActivityMain, "Connection Loss", Toast.LENGTH_SHORT).show()
                setConnection(false)
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                Toast.makeText(this@ActivityMain, "Message delivered", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun subscribeTopic(isFirst : Boolean, qos: Int = 0) {

        spinner_patient.visibility = View.GONE
        topic1 =  applicationContext.getSharedPreferences("MQTT", Context.MODE_PRIVATE).getString("mqttTopic1", "")
        topic2 =  applicationContext.getSharedPreferences("MQTT", Context.MODE_PRIVATE).getString("mqttTopic2", "")
        if (isFirst) mChart.removeAllSeries()
        client.subscribe(topic1, qos).actionCallback = object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2.toFloat()
                paint.color = resources.getColor(R.color.colorPrimaryDark)

                mSeries = LineGraphSeries()
                mSeries.setCustomPaint(paint)
                mChart.addSeries(mSeries)
                btn_subs.visibility = View.GONE
                cv_patient.visibility = View.VISIBLE
                cv_graph.visibility = View.VISIBLE
            }

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                Toast.makeText(this@ActivityMain, "An error occurred", Toast.LENGTH_SHORT).show()
                exception.printStackTrace()
            }
        }
        client.subscribe(topic2, qos).actionCallback = object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {}

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable) {
                Toast.makeText(this@ActivityMain, "An error occurred", Toast.LENGTH_SHORT).show()
                exception.printStackTrace()
            }

        }
    }

    private fun unsubscribeTopic(){
        try {
            topic1 =  applicationContext.getSharedPreferences("MQTT", Context.MODE_PRIVATE).getString("mqttTopic1", "")
            client.unsubscribe(topic1).actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {}

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Toast.makeText(this@ActivityMain, "An error occurred", Toast.LENGTH_SHORT).show()
                }
            }
            client.unsubscribe(topic2).actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {}

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Toast.makeText(this@ActivityMain, "An error occurred", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    private fun initView(isFirst: Boolean){
        if (isFirst) {
            connectMQTT()
            spinner_patient.visibility = View.GONE
        }else {
            i = 0
            unsubscribeTopic()
            spinner_patient.visibility = View.VISIBLE
            adapterResult.removeAll()
        }
        cv_result.visibility = View.GONE
        cv_patient.visibility = View.GONE
        cv_graph.visibility = View.GONE
        btn_subs.visibility = View.GONE

        mChart = findViewById(R.id.graph_bpm)
        mChart.viewport.isXAxisBoundsManual = true
        mChart.viewport.setMinX(0.0)
        mChart.viewport.setMaxX(1600.0)
        mChart.viewport.isYAxisBoundsManual = true
        mChart.viewport.setMaxY(0.5)
        mChart.viewport.setMinY(-1.5)
        mChart.viewport.isScrollable = true
        mChart.viewport.isScalable = true

        mChart.gridLabelRenderer.isHorizontalLabelsVisible = false
        mChart.gridLabelRenderer.textSize = 26f
        mChart.gridLabelRenderer.verticalLabelsColor = resources.getColor(R.color.colorAccent)
        mChart.gridLabelRenderer.labelsSpace = 20
        mChart.gridLabelRenderer.reloadStyles()
        mChart.gridLabelRenderer.isHighlightZeroLines = false
        mChart.gridLabelRenderer.gridStyle = GridLabelRenderer.GridStyle.HORIZONTAL

        spinner_patient.setSelection(0)
    }

    private fun setConnection(conn : Boolean){
        if (conn) {
            isConnected = true
            view_fill.visibility = View.GONE
            view_bottom.visibility = View.VISIBLE
            tx_not_connect.visibility = View.GONE
            tx_reconnect.visibility = View.GONE
        }else{
            isConnected = false
            btn_subs.visibility = View.GONE
            cv_result.visibility = View.GONE
            cv_patient.visibility = View.GONE
            cv_graph.visibility = View.GONE
            view_fill.visibility = View.VISIBLE
            view_bottom.visibility = View.GONE
            tx_not_connect.visibility = View.VISIBLE
            tx_reconnect.visibility = View.VISIBLE
        }
        cv_result.visibility = View.GONE
    }

    private fun close() {
        if(isConnected) {
            client.apply {
                unregisterResources()
                close()
            }
        }
    }

    private fun generatePatient() : ArrayList<Patient>{
        var list_ = ArrayList<Patient>()
        list_.add(Patient("Muhammad Arif", "18", "Male"))
        list_.add(Patient("Malik Anhar Maulana", "21", "Male"))
        list_.add(Patient("Raihan Hamid Suraperwata", "20", "Male"))
        list_.add(Patient("Maurice Fickry", "23", "Male"))
        return list_
    }

    private fun getList_nama(list_patient : ArrayList<Patient>) : ArrayList<String>{
        var list_ = ArrayList<String>()
        list_patient.forEach {
            list_.add(it.name)
        }
        return list_
    }
}