package com.hbe.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.hbe.bluetooth.HBEBTListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;



@SuppressLint({"HandlerLeak"})
public class SPPService implements HBEBTListener {




    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_NAME = "device_name";
    private static final UUID SPP_UUID = UUID.fromString("00000003-0000-1000-8000-00805f9b34fb");
    private final BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState = 0;
    private HBEBTListener mContext;
    public static final int STATE_NONE = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_CON_FAILED = 3;
    public static final int STATE_CON_LOST = 4;
    public static final int STATE_RECEIVE = 5;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case 0:




                    if(SPPService.this.mContext != null) {
                        SPPService.this.onDisconnected();
                    }
                    break;
                case 1:
                    if(SPPService.this.mContext != null) {
                        SPPService.this.onConnecting();
                    }
                    break;
                case 2:
                    if(SPPService.this.mContext != null) {
                        SPPService.this.onConnected();
                    }
                    break;
                case 3:
                    if(SPPService.this.mContext != null) {
                        SPPService.this.onConnectionFailed();
                    }
                    break;
                case 4:
                    if(SPPService.this.mContext != null) {
                        SPPService.this.onConnectionLost();
                    }
                    break;
                case 5:
                    if(SPPService.this.mContext != null) {
                        SPPService.this.onReceive((byte[])msg.obj);
                    }
            }

        }
    };

    public SPPService(Context context) {
        this.mContext = (HBEBTListener)context;
    }

    private synchronized void setState(int state) {
        this.mState = state;
        this.mHandler.obtainMessage(state).sendToTarget();
    }

    private synchronized void setReceive(byte[] buff) {
        this.mHandler.obtainMessage(5, buff.clone()).sendToTarget();
    }

    protected synchronized int getState() {
        return this.mState;
    }

    protected synchronized void start() {
        if(this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }

        if(this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }

        this.setState(0);
    }

    protected synchronized void connect(BluetoothDevice device) {
        if(this.mState == 1 && this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }

        if(this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }

        this.mConnectThread = new ConnectThread(device);
        this.mConnectThread.start();
        this.setState(1);
    }

    protected synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if(this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }

        if(this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }

        this.mConnectedThread = new ConnectedThread(socket);
        this.mConnectedThread.setPriority(1);
        this.mConnectedThread.start();
        Log.e("adk", "started ");
        this.setState(2);
    }

    protected synchronized void disconnect() {
        this.stop();
    }

    protected synchronized void stop() {
        if(this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }

        if(this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }

        this.setState(0);
    }

    public void onConnected() {
        if(this.mContext != null) {
            this.mContext.onConnected();
        }

    }

    public void onDisconnected() {
        if(this.mContext != null) {
            this.mContext.onDisconnected();
        }

    }

    public void onConnecting() {
        if(this.mContext != null) {
            this.mContext.onConnecting();
        }

    }

    public void onConnectionFailed() {
        if(this.mContext != null) {
            this.mContext.onConnectionFailed();
        }

    }

    public void onConnectionLost() {
        if(this.mContext != null) {
            this.mContext.onConnectionLost();
        }

    }

    public void onReceive(byte[] buff) {
        if(this.mContext != null) {
            this.mContext.onReceive(buff);
        }

    }

    protected void sendData(byte[] buff) {
        ConnectedThread r;
        synchronized(this) {
            if(this.mState != 2) {
                return;
            }

            r = this.mConnectedThread;
        }

        r.writeData(buff);
    }

    protected void setListener(HBEBTListener l) {
        this.mContext = l;
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            this.mmDevice = device;
            BluetoothSocket tmp = null;
            Method m = null;

            try {
                m = device.getClass().getMethod("createRfcommSocket", new Class[]{Integer.TYPE});
            } catch (NoSuchMethodException var9) {
                var9.printStackTrace();
            }

            try {
                tmp = (BluetoothSocket)m.invoke(device, new Object[]{Integer.valueOf(1)});
            } catch (IllegalAccessException var6) {
                var6.printStackTrace();
            } catch (IllegalArgumentException var7) {
                var7.printStackTrace();
            } catch (InvocationTargetException var8) {
                var8.printStackTrace();
            }

            Log.e("adk", "connect ");
            this.mmSocket = tmp;
        }

        public void run() {
            this.setName("ConnectThread");
            SPPService.this.mAdapter.cancelDiscovery();

            try {
                this.mmSocket.connect();
                Log.e("adk", "connecting ");
            } catch (IOException var5) {
                Log.e("adk", "err2 ");
                SPPService.this.setState(3);

                try {
                    this.mmSocket.close();
                } catch (IOException var3) {
                    var5.printStackTrace();
                    Log.e("adk", "err1 ");
                }

                SPPService.this.start();
                Log.e("adk", "start ");
                return;
            }

            SPPService e = SPPService.this;
            synchronized(SPPService.this) {
                SPPService.this.mConnectThread = null;
            }

            SPPService.this.connected(this.mmSocket, this.mmDevice);
        }

        public void cancel() {
            try {
                this.mmSocket.close();
            } catch (IOException var2) {
                var2.printStackTrace();
            }

        }
    }

    private class ConnectedThread extends Thread {
        private BluetoothSocket mmSocket;
        private InputStream mmInStream = null;
        private OutputStream mmOutStream = null;
        private boolean mmThreadRun;

        public ConnectedThread(BluetoothSocket socket) {
            this.mmSocket = socket;

            try {
                this.mmInStream = socket.getInputStream();
                this.mmOutStream = socket.getOutputStream();
            } catch (IOException var4) {
                var4.printStackTrace();
            }

        }

        public void run() {
            byte[] buffer = new byte[1];
            this.mmThreadRun = true;

            while(this.mmThreadRun) {
                try {
                    this.mmInStream.read(buffer);
                    SPPService.this.setReceive((byte[])buffer.clone());
                } catch (IOException var3) {
                    SPPService.this.setState(4);
                    Log.e("adk", "err3 ");
                    var3.printStackTrace();
                    break;
                }
            }

        }

        public void writeData(byte[] packet) {
            try {
                for(int e = 0; e < packet.length; ++e) {
                    this.mmOutStream.write(packet[e]);
                    Thread.sleep(5L);
                }
            } catch (IOException var3) {
                var3.printStackTrace();
            } catch (InterruptedException var4) {
                var4.printStackTrace();
            }

        }

        public void cancel() {
            try {
                this.mmInStream.close();
                this.mmOutStream.close();
                this.mmSocket.close();
                this.mmThreadRun = false;
            } catch (IOException var2) {
                var2.printStackTrace();
            }

        }
    }
}
