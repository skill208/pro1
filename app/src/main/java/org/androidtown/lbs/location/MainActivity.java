package org.androidtown.lbs.location;

import android.database.Cursor;
import android.speech.tts.TextToSpeech;
import android.widget.EditText;
import android.Manifest;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import android.widget.*;


import android.database.sqlite.SQLiteDatabase;
import android.app.Activity;
import android.content.ContentValues;
import android.media.*;
import com.hbe.bluetooth.HBEBT;
import com.hbe.bluetooth.HBEBTListener;
//import com.hbe.ultrasonic.R;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import android.widget.CompoundButton;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import android.speech.tts.TextToSpeech;

import android.speech.tts.TextToSpeech.OnInitListener;
import static java.sql.DriverManager.println;

/**
 * 위치 관리자를 이용해 내 위치를 확인하는 방법에 대해 알 수 있습니다.
 *
 * @author Mike
 */
public class MainActivity extends AppCompatActivity implements HBEBTListener, OnInitListener {
    //private TextToSpeech mTts;
    private TextToSpeech myTTS;
    private static String TAG = "MainActivity";


    private static String DATABASE_NAME = "databaseName";
    private static String TABLE_NAME = "tableName";

    TextView contentsText;
    Geocoder gc;

    TextView status;

    SQLiteDatabase db;
   static int tm;
    Button   Set;
    private byte mRecvbuf[] = new byte[8];
    private int mRecvPt = 0;
    private HBEBT mDriver;
    //CheckBox SensorReceiver;
    ImageButton SensorReceiver;
    ImageButton SensorOff;
    ImageButton Sensor;
    EditText GroupId;
    int temp;

    private TextView TextView, GroupIDText;
    ImageView safeView;
    ImageView dangerView;
    int i = 0;

    int gpsdbcount=1;

    boolean databaseCreated = false;
    boolean tableCreated = false;

    //   private SoundPool sound_pool;
    //   private int sound_beep;

    SoundPool sp;
    int soundID;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myTTS = new TextToSpeech(this, this);
        mDriver = new HBEBT(this);
        mDriver.setListener(this);
        TextView = (TextView) findViewById(R.id.textView1);
        Init(0);
        Sensor = (ImageButton) findViewById(R.id.imageButton3);
        SensorReceiver = (ImageButton) findViewById(R.id.imageButton);
        SensorOff = (ImageButton) findViewById(R.id.imageButton2);
        Set = (Button) findViewById(R.id.Set);
        Set.setOnClickListener(new CL());
        GroupId = (EditText) findViewById(R.id.groupId);
        GroupIDText = (TextView) findViewById(R.id.groupidText);
        SensorReceiver.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onInit3(1);
                //센서 거리 나오게
                ReceiveOn(1);



            }

            //         if(i==1) {


            //      }

        });
        SensorOff.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onInit4(1);
                //센서 거리 끄기
                ReceiveOn(0);



            }

            //         if(i==1) {


            //      }

        });


        /*SensorReceiver.setOnClickListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SensorReceiver.setText("ON");
                    ReceiveOn(1);
                } else {
                    SensorReceiver.setText("STOP");
                    ReceiveOn(0);
                }
            }
        });*/


        contentsText = (TextView) findViewById(R.id.contentsText);

       // safeView = (ImageView) findViewById(R.id.imageView1);
      //  dangerView = (ImageView) findViewById(R.id.imageView2);

       // status = (TextView) findViewById(R.id.contentsText2);

      //
          sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
       //  sp = new SoundPool(1, AudioManager.STREAM_ALARM, 0);
        soundID = sp.load(getApplicationContext(), R.raw.hangouts_incoming_call, 1);
     //   soundID = sp.load(getApplicationContext(), R.raw.Alarm1, 1);
        // 버튼 이벤트 처리
        ImageButton button01 = (ImageButton) findViewById(R.id.imageButton4);
        //Button button01 = (Button) findViewById(R.id.button01);
        button01.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // 위치 정보 확인을 위해 정의한 메소드 호출

                startLocationService();
                gpsdbcount--;



            }

            //         if(i==1) {


            //      }

        });

        Button button02 = (Button) findViewById(R.id.button02);
        button02.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                createDatabase(DATABASE_NAME);
                executeRawQuery();
            }

        });

        Button button03 = (Button) findViewById(R.id.button03);
        button03.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                createDatabase(DATABASE_NAME);
                deleteRecord(TABLE_NAME);
            }

        });

        checkDangerousPermissions();


        gc = new Geocoder(this, Locale.KOREAN);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void checkDangerousPermissions() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (int i = 0; i < permissions.length; i++) {
            permissionCheck = ContextCompat.checkSelfPermission(this, permissions[i]);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                break;
            }
        }

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "권한 있음", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "권한 없음", Toast.LENGTH_LONG).show();

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                Toast.makeText(this, "권한 설명 필요함.", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, permissions[i] + " 권한이 승인됨.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, permissions[i] + " 권한이 승인되지 않음.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    /**
     * 위치 정보 확인을 위해 정의한 메소드
     */
    private void startLocationService() {
        // 위치 관리자 객체 참조
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // 위치 정보를 받을 리스너 생성
        GPSListener gpsListener = new GPSListener();
        long minTime = 10000;
        float minDistance = 0;

        try {
            // GPS를 이용한 위치 요청
            manager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minTime,
                    minDistance,
                    gpsListener);

            // 네트워크를 이용한 위치 요청
            manager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    minTime,
                    minDistance,
                    gpsListener);

            // 위치 확인이 안되는 경우에도 최근에 확인된 위치 정보 먼저 확인
            Location lastLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation != null) {
                Double latitude = lastLocation.getLatitude();
                Double longitude = lastLocation.getLongitude();
            //    sp.play(soundID, 1, 1, 0, 0, 1);
                searchLocation(latitude, longitude);
                Toast.makeText(getApplicationContext(), "Last Known Location : " + "Latitude : " + latitude + "\nLongitude:" + longitude, Toast.LENGTH_LONG).show();
            }
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }

        Toast.makeText(getApplicationContext(), "위치 확인이 시작되었습니다. 로그를 확인하세요.", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://org.androidtown.lbs.location/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://org.androidtown.lbs.location/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    /**
     * 리스너 클래스 정의
     */
    private class GPSListener implements LocationListener {
        /**
         * 위치 정보가 확인될 때 자동 호출되는 메소드
         */
        public void onLocationChanged(Location location) {
            if(gpsdbcount==0) {
                Double latitude = location.getLatitude();
                Double longitude = location.getLongitude();

                String msg = "Latitude : " + latitude + "\nLongitude:" + longitude;
                Log.i("GPSListener", msg);
                searchLocation(latitude, longitude);
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                createDatabase(DATABASE_NAME);

                createTable(TABLE_NAME);

                insertRecord(TABLE_NAME, latitude, longitude);

                gpsdbcount++;
            }


        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    }

    /**
     * 위치 좌표를 이용해 주소를 검색하는 메소드 정의
     */
    private void searchLocation(double latitude, double longitude) {
        List<Address> addressList = null;
    String a;
        try {
            addressList = gc.getFromLocation(latitude, longitude, 1);

            if (addressList != null) {
                contentsText.setText("");
                //contentsText.append("\nCount of Addresses for [" + latitude + ", " + longitude + "] : " + addressList.size());
                for (int i = 0; i < addressList.size(); i++) {
                    Address outAddr = addressList.get(i);
                    int addrCount = outAddr.getMaxAddressLineIndex() + 1;
                    StringBuffer outAddrStr = new StringBuffer();
                    for (int k = 0; k < addrCount; k++) {
                        outAddrStr.append(outAddr.getAddressLine(k));
                    }
                  //  outAddrStr.append("\n\tLatitude : " + outAddr.getLatitude());
                    //outAddrStr.append("\n\tLongitude : " + outAddr.getLongitude());

                    contentsText.append("\n\t #" + i + " : " + outAddrStr.toString());
                   a= outAddrStr.toString();
                    onInit2(a);
                }
            }

        } catch (IOException ex) {
            Log.d(TAG, "예외 : " + ex.toString());
        }


    }


    private void createDatabase(String name) {
//        println("creating database [" + name + "].");

        try {
            db = openOrCreateDatabase(name, Activity.MODE_PRIVATE, null);

            databaseCreated = true;
//            println("database is created.");
        } catch (Exception ex) {
            ex.printStackTrace();
//            println("database is not created.");
        }
    }


    private void createTable(String name) {
//        println("creating table [" + name + "].");

        try {

            if (db != null) {
                db.execSQL("create table if not exists  " + name + "("
                        + " latitude double, "
                        + " longitude double)");

                tableCreated = true;
            } else {
//               println("데이터베이스를 먼저 열어야 합니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertRecord(String name, double latitude, double longitude) {
//           println("inserting records into table " + name + ".");

        try {

            if (db != null) {
                db.execSQL("insert into " + name + "(latitude,longitude) values (" +
                        +latitude + "," +
                        +longitude + ")");
                //               println("데이터를 추가했습니다.");
            } else {
//                println("데이터베이스를 먼저 열어야 합니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void executeRawQuery() {
        //      println("\nexecuteRawQuery called.\n");
        status.setText("");
        try {

            if (db != null) {
                Cursor c1 = db.rawQuery("select latitude, longitude as Total from " + TABLE_NAME, null);
                //       println("cursor count : " + c1.getCount());
                int count = c1.getCount();
                println("결과 레코드의 개수: " + count);

                for (int i = 0; i < count; i++) {
                    c1.moveToNext();
                    double latitude = c1.getDouble(0);
                    double longitude = c1.getDouble(1);

                    println("레코드 #" + i + " : " + latitude + "," + longitude);
                }
                c1.close();
                //              println("데이터를 조회했습니다.");
            } else {
                //               println("데이터베이스를 먼저 열어야 합니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //       c1.moveToNext();
        //     println("record count : " + c1.getInt(0));

        //       c1.close();

    }

    private void deleteRecord(String name) {
//       println("inserting records into table " + name + ".");

        try {

            if (db != null) {
                db.execSQL("delete from " + name );
                //               println("데이터를 추가했습니다.");
            } else {
//                println("데이터베이스를 먼저 열어야 합니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void println(String msg) {
        Log.d(TAG, msg);
        status.append("\n" + msg);

    }





    class CL implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.Set:
                    String id = GroupId.getText().toString();
                    if (!id.matches("")) {
                        temp = Integer.parseInt(id);
                        if (temp < 50 && temp > 9) {
                            GroupIDText.setText("Group ID :  Set : [" + temp + "]  ");
                            Toast.makeText(getBaseContext(), "Set", Toast.LENGTH_SHORT).show();
                        } else {
                            GroupIDText.setText("Failed : 10 ~ 49");
                            Toast.makeText(getBaseContext(), "Failed to set", Toast.LENGTH_SHORT).show();
                            temp = 0;
                        }
                    } else {
                        GroupIDText.setText("Failed : 10 ~ 49");
                        Toast.makeText(getBaseContext(), "Failed to set", Toast.LENGTH_SHORT).show();
                        temp = 0;
                    }
                    break;
                default:
                    Toast.makeText(getBaseContext(), "default", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }


    private void ReceiveOn(int t) {
        byte[] buffer = new byte[8];
        buffer[0] = 0x76;
        buffer[1] = 0x00;
        buffer[2] = 0x40;
        buffer[3] = 0x00;
        buffer[4] = (byte) t;
        buffer[5] = 0x00;
        buffer[6] = 0x00;
        buffer[7] = getCSC(buffer);
        sendData(buffer);

        Log.e("test", "0");
    }

    @Override
    public void onDestroy() {
        mDriver.disconnect();

        super.onDestroy();
        myTTS.shutdown();
    }

    private byte getCSC(byte[] buff) {
        int csc = 0;
        for (int i = 2; i < buff.length - 1; i++) {
            csc += buff[i];
        }
        return (byte) (csc & 0xFF);
    }

    public void Init(int i) {
        TextView.setText("Not Connected");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().equals("Connect")) {
            if (temp < 50 && temp > 9) {
                mDriver.conntect(temp);
            } else {
                Toast.makeText(getBaseContext(), "Failed, Check GroupID", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.add("Connect");
        return true;
    }

    @Override
    public void onConnected() {
        Toast.makeText(this, "connected", Toast.LENGTH_SHORT).show();
        TextView.setText("Receive OFF State");
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(this, "disconnected", Toast.LENGTH_SHORT).show();
        Init(0);
    }

    @Override
    public void onConnecting() {

        Toast.makeText(this, "connecting", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed() {
        Toast.makeText(this, "conection failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionLost() {
        Toast.makeText(this, "connection lost", Toast.LENGTH_SHORT).show();
    }

    public void sendData(byte[] buff) {
        // TODO Auto-generated method stub
        if (mDriver != null) mDriver.sendData(buff);
    }

    @Override
    public void onReceive(byte[] bytes) {
        byte values[] = bytes;

        for (int i = 0; i < bytes.length; i++) {
            if (mRecvPt >= 8) { // over flow
                mRecvPt = 0;
                if (values[i] == 0x76)
                    mRecvbuf[mRecvPt++] = values[i];
            } else if (values[i] == 0x76 && mRecvPt == 0) {
                mRecvbuf[mRecvPt++] = values[i];
            } else if (values[i] == 0x76 && mRecvPt > 0) {     // start fix to 0
                if (mRecvbuf[0] == 0x76 && mRecvbuf[1] == 0x00) {
                    mRecvbuf[mRecvPt++] = values[i];
                } else {
                    mRecvPt = 0;
                    mRecvbuf[mRecvPt++] = values[i];
                }
            } else {
                mRecvbuf[mRecvPt++] = values[i];
                if (mRecvPt == 8 && (((mRecvbuf[2] + mRecvbuf[4] + mRecvbuf[5] + mRecvbuf[6]) & 0xff) == (mRecvbuf[7] & 0xff))) { // sensor packet check
                    DisplayValues(mRecvbuf[2], mRecvbuf[4], mRecvbuf[5]);

                }
            }
        }
    }

    private void DisplayValues(byte type, byte temp1, byte temp2) {


        int tmp;
        switch (type) {
            case 0x41:
                int temp;

                temp = temp1 & 0xff;
                temp = (temp << 8 | temp2 & 0xff);
                tmp = temp;/*
                Sensor.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {


                        onInit5(tmp);

                    }

                });*/
                if(temp>10&&temp<=100){
                    sp.play(soundID, 1, 1, 0, 0, 1);
                    //onInit2(1);
                    //for(int i=0;i<1000000000;i++){}
                    try {

                        Toast toastView = Toast.makeText(getApplicationContext(),
                                "주위에 장애물들이 있습니다!",
                                Toast.LENGTH_LONG);



                        // 입력된 x, y offset 값을 이용해 위치를 지정합니다.
                        toastView.setGravity(Gravity.CENTER, 0, 120);

                        toastView.show();

                    } catch (NumberFormatException ex) {
                        Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                    }
                  //  safeView.setImageResource(R.drawable.base);
                   // dangerView.setImageResource(R.drawable.danger);

                    TextView.setText("Ultra : "+temp);
                }

                else if(temp>300) {

                   // safeView.setImageResource(R.drawable.safe);
                   // dangerView.setImageResource(R.drawable.base);
                    TextView.setText("Ultra : "+temp);
                }/*
                else if(temp>1&&temp<20){







                    TextView.setText("Ultra : "+temp);
                }*//*
                else if(temp==0){
                    startLocationService();
                    gpsdbcount=0;


                    TextView.setText("Ultra : "+temp);

                }*/
                else
                    TextView.setText("Ultra : "+temp);

                break;
        }



    }
    public void onInit(int status) {

        String myText1 = "안녕하세요 시각장애인 어플 시장위입니다.";

        String myText2 = "센서와 연결해주세요!";

        myTTS.speak(myText1, TextToSpeech.QUEUE_FLUSH, null);

        myTTS.speak(myText2, TextToSpeech.QUEUE_ADD, null);

    }
    public void onInit2(String a) {

        String myText1 = "현재위치는"+a+"입니다";

        //String myText2 = "말하는 스피치 입니다.";

        myTTS.speak(myText1, TextToSpeech.QUEUE_FLUSH, null);

       //
        //myTTS.speak(myText2, TextToSpeech.QUEUE_ADD, null);

    }
    public void onInit3(int status) {

        String myText1 = "센서 작동";

        myTTS.speak(myText1, TextToSpeech.QUEUE_FLUSH, null);

       // myTTS.speak(myText2, TextToSpeech.QUEUE_ADD, null);

    }
    public void onInit4(int status) {

        String myText1 = "센서 꺼짐";

        myTTS.speak(myText1, TextToSpeech.QUEUE_FLUSH, null);

        // myTTS.speak(myText2, TextToSpeech.QUEUE_ADD, null);

    }

    public void onInit5(int status) {

        String myText1 = "현재 거리는" +status;

        myTTS.speak(myText1, TextToSpeech.QUEUE_FLUSH, null);

        // myTTS.speak(myText2, TextToSpeech.QUEUE_ADD, null);

    }







}
