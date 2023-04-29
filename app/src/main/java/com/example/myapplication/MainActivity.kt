package com.example.myapplication

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Toast
import java.time.LocalDateTime
import java.util.Calendar
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.app.Activity



const val DISCOVERABLE_DURATION = 300
private const val REQUEST_DISCOVERABLE_CODE = 2
private const val PERMISSION_REQUEST_CODE = 1




class MainActivity : AppCompatActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var agreeButton: Button
    private lateinit var locationManager: LocationManager
    private val devices = mutableListOf<BluetoothDevice>()
    private lateinit var deviceListAdapter: DeviceListAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        agreeButton = findViewById(R.id.button_agree)

        // RecyclerViewのアダプターを設定
        deviceListAdapter = DeviceListAdapter(this, devices) { device ->
            // クリックされたデバイスに対して行うアクションをここに記述
        }
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = deviceListAdapter

        // ここで requestDiscoverable 関数を呼び出す
        requestDiscoverable()

        val agreeButton = findViewById<Button>(R.id.button_agree)
        agreeButton.setOnClickListener {
            Log.d("BluetoothAgree", "Agree button clicked")
            val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_ADMIN), PERMISSION_REQUEST_CODE)
            } else {
                // Bluetoothが有効でない場合、有効化するようにユーザーに促す
                if (!bluetoothAdapter.isEnabled) {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                } else {
                    startDeviceDiscovery()
                }
            }

        }


    }

    // 省略...
    companion object {
        private const val REQUEST_ENABLE_BT = 1
    }

    private fun startDeviceDiscovery() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        } else {
            // Bluetoothデバイスの検出を開始する
            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
            startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE_CODE)

            // 検出されたデバイスを処理するレシーバーを登録する
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(receiver, filter)
        }
    }


    // 省略...
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                // 検出されたデバイスを取得する
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                Log.d("BluetoothDiscovery", "Device found: ${device?.name}, ${device?.address}")

                // ここでデバイスに対して行いたい操作を実行できます
                // 例：デバイスの名前やアドレスを表示する
                device?.let {
                    val deviceName = it.name
                    val deviceAddress = it.address
                    Toast.makeText(this@MainActivity, "Device found: $deviceName, $deviceAddress", Toast.LENGTH_SHORT).show()
                    devices.add(device)
                    deviceListAdapter.notifyDataSetChanged()
                    // 合意ボタンが押された際の日時を取得
                    val currentTime = Calendar.getInstance().time

                    // パーミッションが許可されている場合に限り、ロケーションの取得と記録を行います。
                    if (ContextCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        if (location != null) {
                            val latitude = location.latitude
                            val longitude = location.longitude
                            // ここで日時、緯度、経度を記録します。
                            // 例: データベースやファイルに保存、あるいはサーバーに送信するなど
                        }
                    }
                }
            }
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ENABLE_BT -> {
                if (resultCode == Activity.RESULT_OK) {
                    startDeviceDiscovery()
                } else {
                    Toast.makeText(this, "Bluetooth is not enabled", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_DISCOVERABLE_CODE -> {
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Discoverable mode was not enabled", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        // レシーバーの登録を解除
        unregisterReceiver(receiver)
    }
    private fun requestDiscoverable() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                PERMISSION_REQUEST_CODE
            )
        } else {
            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_DURATION)
            startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE_CODE)
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestDiscoverable()
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }




}
