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

package com.loumeng.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.loumeng.Bluetooth.BluetoothChatService;
import com.loumeng.Bluetooth.Data_syn;
import com.loumeng.Bluetooth.DeviceListActivity;
import com.loumeng.Bluetooth.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * 显示通信信息的主Activity。
 */
public class BluetoothChat extends Activity {
	// Debugging
	private static final String TAG = "BluetoothChat";
	private static final boolean D = true;
    //返回页面标志
	private boolean exit =false;

	// 来自BluetoothChatService Handler的消息类型
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	// 来自BluetoothChatService Handler的关键名
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	// Intent请求代码
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;

	// 布局视图
	private TextView mTitle;
	private TextView mConversationView;
	private TextView outcount;
	private TextView incount;

	private TextView view;


	// 声明复选按钮
	private CheckBox in16;
	private CheckBox autosend;
	private CheckBox out16;

	// 声明button按钮
	private Button mSendButton;

	private Button search;
	private Button disc;
	// 用来保存存储的文件名
	public String filename = "";
	// 保存用数据缓存
	private String fmsg = "";
	// 计数用
	private int countin = 0;
	private int countout = 0;

	// 已连接设备的名称
	private String mConnectedDeviceName = null;
	// 输出流缓冲区
	private StringBuffer mOutStringBuffer;

	// 本地蓝牙适配器
	private BluetoothAdapter mBluetoothAdapter = null;
	// 用于通信的服务
	private BluetoothChatService mChatService = null;
	// CheckBox用
	private boolean inhex = true;
	private boolean outhex = true;
	private boolean auto = false;


	@Override
 	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D)
			Log.e(TAG, "+++ ON CREATE +++");
		// 设置窗口布局
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_bluetooth_chat_layout);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);

		//布局控件初始化函数，注册相关监听器
		init();

		// 获取本地蓝牙适配器
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// 如果没有蓝牙适配器，则不支持
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "蓝牙不可用", Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		search.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				search();
			}
		});

		disc.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(BluetoothChat.this, "该设备已设置为可在300秒内发现，且可连接",
						Toast.LENGTH_SHORT).show();
				ensureDiscoverable();
			}
		});
	}

	public void search(){
		Intent serverIntent = new Intent(BluetoothChat.this,
				DeviceListActivity.class);
		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
	}

	private void init(){
		// 通过findViewById获得CheckBox对象
		in16 = (CheckBox) findViewById(R.id.in16);
		autosend = (CheckBox) findViewById(R.id.autosend);
		out16 = (CheckBox) findViewById(R.id.out16);

		// 注册事件监听器
		in16.setOnCheckedChangeListener(listener);
		autosend.setOnCheckedChangeListener(listener);
		out16.setOnCheckedChangeListener(listener);
		// 获得button的对象
		search = (Button) findViewById(R.id.search);
		disc = (Button) findViewById(R.id.discoverable1);

		mSendButton = (Button) findViewById(R.id.button_send);
		//获取选择控件的值

		// 设置custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.activity_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);
		view = (TextView) findViewById(R.id.edit_text_out);

	}


	   // 响应事件监听
	private OnCheckedChangeListener listener = new OnCheckedChangeListener() {

		   @Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			// in16被选中
			if (buttonView.getId() == R.id.in16) {
				if (isChecked) {
					Toast.makeText(BluetoothChat.this, "16进制显示",
							Toast.LENGTH_SHORT).show();
					inhex = true;
				} else
					inhex = false;
			}

			//out16选中
			if (buttonView.getId() == R.id.out16) {
				if (isChecked) {
					Toast.makeText(BluetoothChat.this, "16进制发送",
							Toast.LENGTH_SHORT).show();
					outhex = true;
				} else
					outhex = false;
			}
			// 自动发送被选中
			if (buttonView.getId() == R.id.autosend) {
				if (isChecked) {
					Toast.makeText(BluetoothChat.this, "自动发送",
							Toast.LENGTH_SHORT).show();
					auto = true;
				} else
					auto = false;
			}
		}
	};


	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");


		//如果BT未打开，请求启用。
		// 然后在onActivityResult期间调用setupChat（）
		if (!mBluetoothAdapter.isEnabled())
		{
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// 否则，设置聊天会话
		}
		else
		{

			if (mChatService == null)
				setupChat();
			else {
				try {
					mChatService.wait(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}


	//300秒内搜索
	private void ensureDiscoverable() {
		if (D)
			Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			//设置本机蓝牙可让发现
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	//自动发送
	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			String message = view.getText().toString();
			sendMessage(message);
			// 初始化输出流缓冲区
			mOutStringBuffer = new StringBuffer("");

		}
	};


//初始化
	private void setupChat() {
		Log.d(TAG, "setupChat()");

		mConversationView = (TextView) findViewById(R.id.in);
		mConversationView.setMovementMethod(ScrollingMovementMethod
				.getInstance());// 使TextView接收区可以滚动
		outcount = (TextView) findViewById(R.id.outcount);
		incount = (TextView) findViewById(R.id.incount);
		outcount.setText("0");
		incount.setText("0");

		mSendButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// 使用编辑文本小部件的内容发送消息
				TextView view = (TextView) findViewById(R.id.edit_text_out);

					String message = view.getText().toString();
					sendMessage(message);

			}
		});

			// 初始化BluetoothChatService以执行app_incon_bluetooth连接
		mChatService = new BluetoothChatService(this, mHandler);

		//初始化外发消息的缓冲区
		mOutStringBuffer = new StringBuffer("");
	}


	//重写发送函数，参数不同。
	private void sendMessage(String message) {
		// 确保已连接
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		// 检测是否有字符串发送
		if (message.length() > 0) {
			// 获取 字符串并告诉BluetoothChatService发送
			if (outhex == true) {
				byte[] send = Data_syn.hexStr2Bytes(message);
				mChatService.write(send);//回调service

			} else if (outhex == false) {
				byte[] send = message.getBytes();
				mChatService.write(send);//回调service
			}
			// 清空输出缓冲区
			mOutStringBuffer.setLength(0);
		}
		else {
			Toast.makeText(this,"发送内容不能为空",
					Toast.LENGTH_SHORT).show();
		}
	}





	// 该Handler从BluetoothChatService中获取信息
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MESSAGE_STATE_CHANGE:
					if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);


					switch (msg.arg1)
					{
						case BluetoothChatService.STATE_CONNECTED:
					     mTitle.setText(R.string.title_connected_to);
					     mTitle.append(mConnectedDeviceName);
					     mConversationView.setText(null);
					     break;

						case BluetoothChatService.STATE_CONNECTING:
					    mTitle.setText(R.string.title_connecting);
					    break;

						case BluetoothChatService.STATE_LISTEN:
						case BluetoothChatService.STATE_NONE:
					    mTitle.setText(R.string.title_not_connected);
					    break;
				   }
				break;

			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// 自动发送
				if (auto == true) {

						// 自动发送模块
					mHandler.postDelayed(runnable, 1000);
					} else if (auto == false) {
						mHandler.removeCallbacks(runnable);
				}
				// 发送计数
				if (outhex == true) {
					String writeMessage = Data_syn.Bytes2HexString(writeBuf);
					countout += writeMessage.length() / 2;
					outcount.setText("" + countout);
				} else if (outhex == false) {
					String writeMessage = null;
					try {
						writeMessage = new String(writeBuf, "GBK");
					} catch (UnsupportedEncodingException e1) {
						e1.printStackTrace();
					}
					countout += writeMessage.length();
					outcount.setText("" + countout);
				}
				break;
			case MESSAGE_READ:

				byte[] readBuf = (byte[]) msg.obj;

				  //检错误码计算函数

				if (inhex == true) {
					String readMessage = " "
							+ Data_syn.bytesToHexString(readBuf, msg.arg1);
					fmsg += readMessage;
					mConversationView.append(readMessage);
					// 接收计数，更显UI
					countin += readMessage.length() / 2;
					incount.setText("" + countin);
				} else if (inhex == false) {
					String readMessage = null;
					try {
						readMessage = new String(readBuf, 0, msg.arg1, "GBK");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					fmsg += readMessage;
					mConversationView.append(readMessage);
					// 接收计数，更新UI
					countin += readMessage.length();
					incount.setText("" + countin);
				}
				break;
			case MESSAGE_DEVICE_NAME:
				// 保存已连接设备的名称
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"连接到 " + mConnectedDeviceName, Toast.LENGTH_SHORT)
						.show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};



    //返回该Activity回调函数
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);

		switch (requestCode) {

//search返回的
		case REQUEST_CONNECT_DEVICE:

			// DeviceListActivity返回时要连接的设备
			if (resultCode == Activity.RESULT_OK) {
				// 获取设备的MAC地址
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);

				// 获取BLuetoothDevice对象
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);
				// 尝试连接到设备
				mChatService.connect(device);
			}
			break;
//start返回的（遇到蓝牙不可用退出）
		case REQUEST_ENABLE_BT:
			// 当启用蓝牙的请求返回时
			if (resultCode == Activity.RESULT_OK)
			{
				//蓝牙已启用，因此设置聊天会话
				setupChat();//初始化文本
			}
			else
			{
				// 用户未启用蓝牙或发生错误
				Log.d(TAG, "BT not enabled");

				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}


	// 保存按键响应函数
	public void onSaveButtonClicked(View v) {
		Save();
	}

	// 清屏按键响应函数
	public void onClearButtonClicked(View v) {
		fmsg = "";
		mConversationView.setText(null);
		view.setText(null);
		return;
	}

	// 清除计数按键响应函数
	public void onClearCountButtonClicked(View v) {
		countin = 0;
		countout = 0;
		outcount.setText("0");
		incount.setText("0");
		return;
	}



	// 保存功能实现
	private void Save() {
		// 显示对话框输入文件名
		LayoutInflater factory = LayoutInflater.from(BluetoothChat.this); // 图层模板生成器句柄
		final View DialogView = factory.inflate(R.layout.sname, null); // 用sname.xml模板生成视图模板
		new AlertDialog.Builder(BluetoothChat.this).setTitle("文件名")
				.setView(DialogView) // 设置视图模板
				.setPositiveButton("确定", new DialogInterface.OnClickListener() // 确定按键响应函数
						{
							public void onClick(DialogInterface dialog,
									int whichButton) {
								EditText text1 = (EditText) DialogView
										.findViewById(R.id.sname); // 得到文件名输入框句柄
								filename = text1.getText().toString(); // 得到文件名

								try {
									if (Environment.getExternalStorageState()
											.equals(Environment.MEDIA_MOUNTED)) { // 如果SD卡已准备好

										filename = filename + ".txt"; // 在文件名末尾加上.txt
										File sdCardDir = Environment
												.getExternalStorageDirectory(); // 得到SD卡根目录
										File BuildDir = new File(sdCardDir,
												"/BluetoothSave"); // 打开BluetoothSave目录，如不存在则生成
										if (BuildDir.exists() == false)
											BuildDir.mkdirs();
										File saveFile = new File(BuildDir,
												filename); // 新建文件句柄，如已存在仍新建文档
										FileOutputStream stream = new FileOutputStream(
												saveFile); // 打开文件输入流
										stream.write(fmsg.getBytes());
										stream.close();
										Toast.makeText(BluetoothChat.this,
												"存储成功！", Toast.LENGTH_SHORT)
												.show();
									} else {
										Toast.makeText(BluetoothChat.this,
												"没有存储卡！", Toast.LENGTH_LONG)
												.show();
									}
								} catch (IOException e) {
									return;
								}
							}
						})
				.setNegativeButton("取消", // 取消按键响应函数,直接退出对话框不做任何处理
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).show(); // 显示对话框
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");
		// 在onResume（）中执行此检查包括在onStart（）期间未启用BT的情况，
		// 因此我们暂停启用它...
		// onResume（）将在ACTION_REQUEST_ENABLE活动时被调用返回.
		if (mChatService != null) {
			// 只有状态是STATE_NONE，我们知道我们还没有启动蓝牙
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// 启动BluetoothChat服务
				mChatService.start();
			}
		}

	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		// 停止蓝牙通信连接服务
		if (mChatService != null)
			mChatService.stop();
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
	}
	@Override
	public void onBackPressed()
	{
		exit();
	}

	public void exit()
	{
		exit = true;


		if(exit  ==  true)
		{
			this.finish();
		}

	}
}