package com.example.myapplication

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat


class DeviceListAdapter(private val context: Context, private val devices: List<BluetoothDevice>, private val onClickListener: (BluetoothDevice) -> Unit) : RecyclerView.Adapter<DeviceListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_device, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = devices[position]

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            holder.deviceName.text = device.name ?: "Unknown Device"
        } else {
            holder.deviceName.text = "Unknown Device (no permission)"
        }

        holder.deviceAddress.text = device.address
        holder.itemView.setOnClickListener {
            onClickListener(device)
        }
    }


    override fun getItemCount(): Int {
        return devices.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.findViewById(R.id.device_name)
        val deviceAddress: TextView = itemView.findViewById(R.id.device_address)
    }
}
