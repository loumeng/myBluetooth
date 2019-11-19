/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.loumeng.Bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;



import java.util.Set;



/**
 * 这个 Activity显示为一个对话框. 它列出了所有已配对的设备和之后在该地区搜索到的设备. 当一个设备被用户选择后,
 * 该设备的MAC地址会在result Intent中被传回到父 Activity.
 */
public class DeviceListActivity extends Activity
{
	// Debugging
	private static final String TAG = "DeviceListActivity";
	private static final boolean D = true;

	// Return Intent extra
	public static String EXTRA_DEVICE_ADDRESS = "device_address";
	public static String EXTRA_LEGAL = "legal";

	// Member fields
	private BluetoothAdapter mBtAdapter;
	private ArrayAdapter<String> mPairedDevicesArrayAdapter;
	private ArrayAdapter<String> mNewDevicesArrayAdapter;

	private boolean isexist =false;
	//自动配对结果
	private boolean result=false;



	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// 设置窗口
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.device_list);

		// 设置结果CANCELED使用户退出
		setResult(Activity.RESULT_CANCELED);


		// 初始化阵列适配器。 一个用于已配对的设备，一个用于新发现的设备,一个用于测试专用的设备
		mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.device_name);
		mNewDevicesArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.device_name);


		// 为配对的设备查找和设置ListView
		ListView pairedListView = (ListView) findViewById(R.id.paired_devices);

		pairedListView.setAdapter(mPairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(mDeviceClickListener);


		// 为新发现的设备找到并设置ListView
		ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);

		newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
		newDevicesListView.setOnItemClickListener(mDeviceClickListener);



		// 发现设备时注册广播
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mReceiver, filter);

		// 发现完成后注册广播
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);

		//自动配对请求注册广播

		filter = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
		this.registerReceiver(mReceiver, filter);

		// 获取本地蓝牙适配器
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();

		// 获取一组当前配对的设备
		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

		// 如果有配对的设备，请将每个设备添加到ArrayAdapter
		TextView textView = (TextView) findViewById(R.id.title_paired_devices);
		if (pairedDevices.size() > 0)
		{
			  textView.setVisibility(View.VISIBLE);
			for (BluetoothDevice device : pairedDevices)
			{   isexist=true;
				mPairedDevicesArrayAdapter.add(device.getName() + "\n"
						+ device.getAddress());

			}
		}

		if (isexist==false)
		{
			textView.setVisibility(View.GONE);
			String noDevices = getResources().getText(R.string.none_paired)
					.toString();
			mPairedDevicesArrayAdapter.add(noDevices);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		doDiscovery();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		// 确保我们不再进行发现
		if (mBtAdapter != null)
		{
			mBtAdapter.cancelDiscovery();
		}

		//取消注册广播侦听器
		this.unregisterReceiver(mReceiver);
	}

	/**
	 * 使用BluetoothAdapter启动设备发现
	 */
	private void doDiscovery()
	{
		if (D) Log.d(TAG, "doDiscovery()");

		// 在标题中指示扫描
		setProgressBarIndeterminateVisibility(true);
		setTitle(R.string.scanning);

		// 开启新装置的副标题
		findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

		// 如果我们已经发现，停止它
		if (mBtAdapter.isDiscovering())
		{
			mBtAdapter.cancelDiscovery();
		}
		//从BluetoothAdapter请求发现
		mBtAdapter.startDiscovery();
	}

	// ListView中其他设备的点击监听器
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3)
		{
			//取消发现,我们m_about来连接
			mBtAdapter.cancelDiscovery();

			// 获取设备的MAC地址，这是视图中最后17个字符
			String info = ((TextView) v).getText().toString();
			String address = info.substring(info.length() - 17);

			// 创建结果意图并包括MAC地址
			Intent intent = new Intent();
			intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
			intent.putExtra(EXTRA_LEGAL,false);
			// 设置结果并完成此活动
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	};
     //ListView中专用测试设备的点击监听器
	private  OnItemClickListener mZYDeviceClickListener = new OnItemClickListener() {
		 @Override
		 public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			 //取消发现,我们m_about来连接
			 mBtAdapter.cancelDiscovery();

			 // 获取设备的MAC地址，这是视图中最后17个字符
			 String info = ((TextView) view).getText().toString();
			 String address = info.substring(info.length() - 17);

			 // 创建结果意图并包括MAC地址
			 Intent intent = new Intent();
			 intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
			 intent.putExtra(EXTRA_LEGAL,true);
			 // 设置结果并完成此活动
			 setResult(Activity.RESULT_OK, intent);
			 finish();
		 }
	 };
	// BroadcastReceiver，侦听发现的设备，并在发现完成时更改标题
	private final BroadcastReceiver mReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
         //从Intent获取BluetoothDevice对象
			BluetoothDevice device = intent
					.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			// 发现找到设备时
			if (BluetoothDevice.ACTION_FOUND.equals(action))
			{    String devicename =device.getName();
				while (devicename==null){
					devicename =device.getName();
				}


				// 该设备没有配对，加载到新的通用蓝牙设备的列表

				if (device.getBondState() != BluetoothDevice.BOND_BONDED)
				{
					mNewDevicesArrayAdapter.add(device.getName() + "\n"
							+ device.getAddress());

				}
			     //	再次得到的action，会等于PAIRING_REQUEST,进行配对操作
			}else if(BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)){
				String devicename =device.getName();
				while (devicename==null){
					devicename =device.getName();
				}
				Log.e(TAG,"接收到配对广播");


			}
			// 发现完成后，更改活动标题
			else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
			{
			setProgressBarIndeterminateVisibility(false);
			setTitle(R.string.select_device);

			if (mNewDevicesArrayAdapter.getCount() == 0)
			{
				String noDevices = getResources().getText(
					R.string.none_found).toString();
				mNewDevicesArrayAdapter.add(noDevices);
			}


		}
		}
	};

}
