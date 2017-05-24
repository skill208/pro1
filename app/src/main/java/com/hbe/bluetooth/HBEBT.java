
package com.hbe.bluetooth;


import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import com.hbe.bluetooth.HBEBTListener;
import com.hbe.bluetooth.SPPService;
import java.util.Iterator;
import java.util.Set;

public class HBEBT extends Dialog implements HBEBTListener {
	public static String EXTRA_DEVICE_ADDRESS = "device_address";
	private HBEBTListener mListener = null;
	private BluetoothAdapter mBluetoothAdapter;
	private SPPService mSPPService;
	private Activity mActivity;
	private HBEBT mHBEBT;
	private BluetoothAdapter mBtAdapter;
	private ArrayAdapter<String> mPairedDevicesArrayAdapter;
	private ArrayAdapter<String> mNewDevicesArrayAdapter;
	private LinearLayout mDeviceListLL;
	private TextView mDeviceListTv1;
	private TextView mDeviceListTv2;
	private ListView mDeviceListLv1;
	private ListView mDeviceListLv2;
	private Button mDeviceListBtn1;

	private int groupID=0;
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			if(HBEBT.this.mBtAdapter.isDiscovering()) {
				HBEBT.this.mBtAdapter.cancelDiscovery();
			}

			String info = ((TextView)v).getText().toString();
			if(!info.equals("No devices have been paired") && !info.equals("No devices found") && info.length() >= 16) {
				String address = info.substring(info.length() - 17);
				Intent intent = new Intent();
				intent.putExtra(HBEBT.EXTRA_DEVICE_ADDRESS, address);
				HBEBT.this.setBluetoothState(-1, "address", address);
			}
		}
	};
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if("android.bluetooth.device.action.FOUND".equals(action)) {
				BluetoothDevice device = (BluetoothDevice)intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
				if(device.getBondState() != 12) {
					if(device.getName()!=null){
						if(device.getName().toString().equals("Arduino"+groupID)){
					HBEBT.this.mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());}
				}}
			} else if("android.bluetooth.adapter.action.DISCOVERY_FINISHED".equals(action)) {
				HBEBT.this.mActivity.setProgressBarIndeterminateVisibility(false);
				HBEBT.this.setTitle("Select a device to connect");
				HBEBT.this.mDeviceListBtn1.setVisibility(View.VISIBLE);
				if(HBEBT.this.mNewDevicesArrayAdapter.getCount() == 0) {
					HBEBT.this.mDeviceListTv2.setVisibility(View.GONE);
				}
			}

		}
	};

	public HBEBT(Activity a) {
		super(a);
		this.mActivity = a;
		this.mHBEBT = this;
		this.createDeviceListLayout();
		this.init();
	}

	public void show() {
		if(this.mActivity != null) {
			if(!this.mBluetoothAdapter.isEnabled()) {
				Intent enableIntent = new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE");
				this.mActivity.startActivity(enableIntent);
			} else {
				this.initArrayAdapter();
				this.setContentView(this.mDeviceListLL);
				this.setBluetoothState(-1, "connect", (String)null);
				if(this.mBtAdapter.isDiscovering()) {
					this.mBtAdapter.cancelDiscovery();
				}

				this.mDeviceListTv2.setVisibility(View.GONE);
				this.mNewDevicesArrayAdapter.clear();
				super.show();
			}
		}
	}

	public void setActivity(Activity a) {
		this.mActivity = a;
	}

	private void setBluetoothState(int resultCode, String id, String address) {
		if(id == "address") {
			if(resultCode == -1) {
				this.mActivity.unregisterReceiver(this.mReceiver);
				BluetoothDevice device = this.mBluetoothAdapter.getRemoteDevice(address);
				this.mSPPService.connect(device);
				this.mHBEBT.hide();
			}
		} else if(id == "connect") {
			if(resultCode == -1) {
				if(this.mSPPService == null) {
					this.mSPPService = new SPPService(this.mActivity);
				}
			} else {
				Toast.makeText(this.mActivity, "Bluetooth was not enabled.", Toast.LENGTH_LONG).show();
				this.mActivity.finish();
			}
		}

	}

	private void init() {
		this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(this.mBluetoothAdapter == null) {
			Toast.makeText(this.mActivity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
			this.mActivity.finish();
		} else {
			if(!this.mBluetoothAdapter.isEnabled()) {
				Intent enableIntent = new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE");
				this.mActivity.startActivity(enableIntent);
			} else if(this.mSPPService == null) {
				this.mSPPService = new SPPService(this.mActivity);
			}

		}
	}

	public void conntect(int temp) {
		groupID=temp;
		this.show();
	}

	public void disconnect() {
		if(this.mSPPService != null) {
			this.mSPPService.disconnect();
			this.mSPPService = null;
		}

		if(this.mBtAdapter != null) {
			this.mBtAdapter.cancelDiscovery();
		}

	}

	public void onConnected() {
		if(this.mListener != null) {
			this.mListener.onConnected();
		}

	}

	public void onDisconnected() {
		if(this.mListener != null) {
			this.mListener.onDisconnected();
		}

	}

	public void onConnecting() {
		if(this.mListener != null) {
			this.mListener.onConnecting();
		}

	}

	public void onReceive(byte[] buff) {
		if(this.mListener != null) {
			this.mListener.onReceive(buff);
		}

	}

	public void onConnectionFailed() {
		if(this.mListener != null) {
			this.mListener.onConnectionFailed();
		}

	}

	public void onConnectionLost() {
		if(this.mListener != null) {
			this.mListener.onConnectionLost();
		}

	}

	public void sendData(byte[] buff) {
		if(this.mSPPService != null) {
			this.mSPPService.sendData(buff);
		}

	}

	public void setListener(HBEBTListener l) {
		this.mListener = l;
	}


	private void createDeviceListLayout(){
		mDeviceListLL = new LinearLayout(mActivity);

		mDeviceListTv1 = new TextView(mActivity);
		mDeviceListLv1 = new ListView(mActivity);
		mDeviceListTv2 = new TextView(mActivity);
		mDeviceListLv2 = new ListView(mActivity);
		mDeviceListBtn1 = new Button(mActivity);

		LayoutParams lp1 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);// entire layout
		LayoutParams lp2 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);// text line ,button
		LayoutParams lp3 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);// paired list
		LayoutParams lp4 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 2.0f);// new list

		setTitle("Select a device to connect");

		mDeviceListLL.setLayoutParams(lp1);
		mDeviceListLL.setWeightSum(10);
		mDeviceListLL.setOrientation(LinearLayout.VERTICAL);

		mDeviceListTv1.setLayoutParams(lp2);
		mDeviceListTv1.setText("Paired Devices");
		mDeviceListTv1.setVisibility(TextView.GONE);
		mDeviceListTv1.setBackgroundColor(Color.rgb(119, 119, 119));
		mDeviceListTv1.setTextColor(Color.rgb(255, 255, 255));
		mDeviceListTv1.setPadding(5, 0, 0, 0);


		mDeviceListLv1.setLayoutParams(lp3);

		mDeviceListLv1.setStackFromBottom(true);

		mDeviceListTv2.setLayoutParams(lp2);
		mDeviceListTv2.setText("Other Available Devices");
		mDeviceListTv2.setVisibility(TextView.GONE);
		mDeviceListTv2.setBackgroundColor(Color.rgb(119, 119, 119));
		mDeviceListTv2.setTextColor(Color.rgb(255,255,255));
		mDeviceListTv2.setPadding(5, 0, 0, 0);

		mDeviceListLv2.setLayoutParams(lp4);
		mDeviceListLv2.setStackFromBottom(true);

		mDeviceListBtn1.setLayoutParams(lp2);
		mDeviceListBtn1.setText("Scan for devices");

		mDeviceListLL.addView(mDeviceListTv1);
		mDeviceListLL.addView(mDeviceListLv1);
		mDeviceListLL.addView(mDeviceListTv2);
		mDeviceListLL.addView(mDeviceListLv2);
		mDeviceListLL.addView(mDeviceListBtn1);

		mDeviceListBtn1.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				doDiscovery();
				v.setVisibility(View.GONE);
			}
		});
	}
	private void initArrayAdapter() {
		mPairedDevicesArrayAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1);
		mNewDevicesArrayAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1);
		ListView pairedListView = this.mDeviceListLv1;
		pairedListView.setAdapter(this.mPairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(this.mDeviceClickListener);
		ListView newDevicesListView = this.mDeviceListLv2;
		newDevicesListView.setAdapter(this.mNewDevicesArrayAdapter);
		newDevicesListView.setOnItemClickListener(this.mDeviceClickListener);
		IntentFilter filter = new IntentFilter("android.bluetooth.device.action.FOUND");
		this.mActivity.registerReceiver(this.mReceiver, filter);
		filter = new IntentFilter("android.bluetooth.adapter.action.DISCOVERY_FINISHED");
		this.mActivity.registerReceiver(this.mReceiver, filter);
		this.mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		Set pairedDevices = this.mBtAdapter.getBondedDevices();
		if(pairedDevices.size() > 0) {
			this.mDeviceListTv1.setVisibility(View.VISIBLE);
			Iterator var6 = pairedDevices.iterator();

			while(var6.hasNext()) {
				BluetoothDevice noDevices = (BluetoothDevice)var6.next();
				if(noDevices.getName().startsWith("A"))
				this.mPairedDevicesArrayAdapter.add(noDevices.getName() + "\n" + noDevices.getAddress());
			}
		} else {
			String noDevices1 = "No devices have been paired";
			this.mPairedDevicesArrayAdapter.add(noDevices1);
		}

	}

	private void doDiscovery() {
		this.mActivity.setProgressBarIndeterminateVisibility(true);
		this.setTitle("Scanning for devices...");
		this.mDeviceListTv2.setVisibility(View.VISIBLE);
		if(this.mBtAdapter.isDiscovering()) {
			this.mBtAdapter.cancelDiscovery();
		}

		this.mBtAdapter.startDiscovery();
	}
}
