package com.example.kilam.inscadeapp

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.kilam.inscadeapp.R.layout.fragment_dashboard
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
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
    private val mReference = FirebaseDatabase.getInstance().reference.child("patient")
    private var list_patient = ArrayList<Patient>()
    private var list_nama = ArrayList<String>()

    private var isConnected : Boolean = false
    private var isPlayed : Boolean = false
    private var i = 0
    private var mqttHost = ""
    private var mqttPort = ""
    private var topic = ""

    var handler : Handler? = null
    internal var milisecTime : Long = 0
    internal var startTime : Long = 0
    internal var timeBuff : Long = 0
    internal var updateTime = 0L
    internal var seconds : Int = 0
    internal var minutes : Int = 0
    internal var milisec : Int = 0
    internal var flag : Boolean = false

    private lateinit var mAdapter : ArrayAdapter<String>
    private lateinit var client : MqttAndroidClient
    private lateinit var mChart : GraphView
    private lateinit var mSeries : LineGraphSeries<DataPoint>
    private lateinit var resultAdapter : AdapterResult

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(fragment_dashboard)

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        initView(true)

        btn_subs.setOnClickListener { subscribeTopic(true, 0) }
        iv_pause_play.setOnClickListener { pause_play(isPlayed) }
        iv_restart.setOnClickListener { restart() }
        iv_reset.setOnClickListener { initView(false) }
        tx_reconnect.setOnClickListener{ connectMQTT() }

        mReference.addChildEventListener(object : ChildEventListener{
            override fun onCancelled(p0: DatabaseError?){}

            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {}

            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {}

            override fun onChildAdded(dataSnapshot: DataSnapshot?, p1: String?) {
                if (dataSnapshot != null){
                    var patient = dataSnapshot.getValue(Patient::class.java)
                    list_patient.add(patient)
                    mAdapter.add(patient.name!!)
                    mAdapter.notifyDataSetChanged()
                }
            }

            override fun onChildRemoved(p0: DataSnapshot?) {}
        })
        list_nama.add("Select Patient")
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
                }
            }
        }
        resultAdapter = AdapterResult()
        rv_result.apply {
            layoutManager = LinearLayoutManager(this@ActivityMain)
            adapter = resultAdapter
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
                if(topic.equals(this@ActivityMain.topic)) {
                    tv_bpm.setText(String(message!!.payload))
                    mSeries.appendData(DataPoint(i.toDouble(), String(message!!.payload).toDouble()), true, 8000)
                    i += 1
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
        handler?.postDelayed(runnable, 0)
        startTime = SystemClock.uptimeMillis()
        flag = true

        spinner_patient.visibility = View.GONE
        topic =  applicationContext.getSharedPreferences("MQTT", Context.MODE_PRIVATE).getString("mqttTopic", "")
        if (isFirst) mChart.removeAllSeries()
        client.subscribe(topic, qos).actionCallback = object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2.toFloat()
                paint.color = resources.getColor(R.color.colorPrimaryDark)

                mSeries = LineGraphSeries()
                mSeries.setCustomPaint(paint)
                mChart.addSeries(mSeries)
                isPlayed = true

                iv_pause_play.setImageResource(R.drawable.ic_pause)
                btn_subs.visibility = View.GONE
                cv_result.visibility = View.VISIBLE
                cv_patient.visibility = View.VISIBLE
                cv_graph.visibility = View.VISIBLE
                cv_timer.visibility = View.VISIBLE
            }

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                Toast.makeText(this@ActivityMain, "An error occurred", Toast.LENGTH_SHORT).show()
                exception.printStackTrace()
            }
        }
    }

    private fun unsubscribeTopic(){
        try {
            flag = false
            topic =  applicationContext.getSharedPreferences("MQTT", Context.MODE_PRIVATE).getString("mqttTopic", "")
            client.unsubscribe(topic).actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {}

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Toast.makeText(this@ActivityMain, "An error occurred", Toast.LENGTH_SHORT).show()
                }
            }
            handler?.removeCallbacks(runnable)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    var runnable : Runnable = object : Runnable{
        override fun run() {
            milisecTime = SystemClock.uptimeMillis() - startTime
            updateTime = timeBuff + milisecTime
            seconds = (updateTime / 1000).toInt()
            minutes = seconds / 60
            seconds %= 60
            milisec = (updateTime % 1000).toInt()

            tv_timer_minute.setText(fillInt(minutes, 2))
            tv_timer_second.setText(fillInt(seconds, 2))
            tv_timer_milisecond.setText(fillInt(milisec, 3))

            handler?.postDelayed(this, 0)

            if(i % 200 == 0){
                resultAdapter.addItem(Result(fillInt(minutes, 2), fillInt(seconds, 2), "Normal"))
                rv_result.smoothScrollToPosition(resultAdapter.itemCount -1)
            }
        }
    }

    private fun fillInt(value : Int, numOfInt : Int) : String{
        if (numOfInt == 2) {
            when (value < 10) {
                true -> return "0$value"
                false -> return value.toString()
            }
        }else{
            if (value < 10) {
                return "00$value"
            }else if (value < 100) {
                return "0$value"
            }else{
                return value.toString()
            }
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
        }
        cv_result.visibility = View.GONE
        cv_patient.visibility = View.GONE
        cv_graph.visibility = View.GONE
        cv_timer.visibility = View.GONE
        btn_subs.visibility = View.GONE

        mChart = findViewById(R.id.graph_bpm)
        mChart.viewport.isXAxisBoundsManual = true
        mChart.viewport.setMinX(0.0)
        mChart.viewport.setMaxX(150.0)
        mChart.viewport.isYAxisBoundsManual = true
        mChart.viewport.setMaxY(2.0)
        mChart.viewport.setMinY(-1.0)
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

        handler = Handler()
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
            cv_timer.visibility = View.GONE
            view_fill.visibility = View.VISIBLE
            view_bottom.visibility = View.GONE
            tx_not_connect.visibility = View.VISIBLE
            tx_reconnect.visibility = View.VISIBLE
        }
    }

    private fun pause_play(played : Boolean){
        if (played){
            iv_pause_play.setImageResource(R.drawable.ic_play)
            unsubscribeTopic()
            isPlayed = false
        }else{
            iv_pause_play.setImageResource(R.drawable.ic_pause)
            subscribeTopic(false, 0)
            isPlayed = true
        }
    }

    private fun restart(){
        unsubscribeTopic()
        mChart.removeAllSeries()
        i = 0
        btn_subs.visibility = View.VISIBLE
        cv_result.visibility = View.GONE
        tv_timer_milisecond.text = "00"
        tv_timer_second.text = "00"
        tv_timer_minute.text = "00"
        tv_bpm.text = "0"
    }

    private fun close() {
        if(isConnected) {
            client.apply {
                unregisterResources()
                close()
            }
        }
    }
}