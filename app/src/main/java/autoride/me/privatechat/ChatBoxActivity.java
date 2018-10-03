package autoride.me.privatechat;

import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Ack;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.net.URISyntaxException;
import java.util.ArrayList;


public class ChatBoxActivity extends AppCompatActivity {
    private Socket socket;
    public String Nickname;
    private ListView list;
    private LinearLayout chatContainer,containerMsgBox;
    private EditText editMessage;
    private Button btnSend;
    private TextView textMessage;
    String name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_box);
        list = (ListView) findViewById(R.id.list);
        chatContainer = (LinearLayout) findViewById(R.id.chatContainer);
        containerMsgBox = (LinearLayout) findViewById(R.id.containerMsgBox);
        editMessage=(EditText)findViewById(R.id.editMessage);
        btnSend=(Button)findViewById(R.id.btnSend);
        textMessage=(TextView)findViewById(R.id.textMessage);
        containerMsgBox.setVisibility(View.GONE);
        // get the nickame of the user
        Nickname = (String) getIntent().getExtras().getString(MainActivity.NICKNAME);
        //connect you socket client to the server
        try {
            socket = IO.socket("http://192.168.0.123:3000");
            socket.connect();
            socket.emit("new user", Nickname, new Ack() {
                @Override
                public void call(Object... args) {
                    boolean response = (Boolean) args[0];
                    if(!response){
                        Snackbar.make(chatContainer,"already user added!",Snackbar.LENGTH_LONG).show();
                    }
                    System.out.print(response);
                }
            });
        } catch (URISyntaxException e) {
            e.printStackTrace();

        }


        //implementing socket listeners
        socket.on("usernames", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONArray data = (JSONArray) args[0];
                        String name = null;
                        ArrayList<String> nameList = new ArrayList<>();
                        try {
                            for (int i = 0; i < data.length(); i++) {
                                name = data.getString(i);
                                nameList.add(name);
                            }
                            ArrayAdapter<String> nameAdapter = new ArrayAdapter<String>(ChatBoxActivity.this, android.R.layout.simple_list_item_1, nameList);
                            list.setAdapter(nameAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        });


        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                containerMsgBox.setVisibility(View.VISIBLE);
                 String ItemName= adapterView.getAdapter().getItem(i).toString();
                 name="/w"+" "+ItemName;
              //  Toast.makeText(ChatBoxActivity.this, "NAME: "+name, Toast.LENGTH_SHORT).show();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg=editMessage.getText().toString().trim();
                String message=name+" "+msg;
                socket.emit("send message", message, new Ack() {
                    @Override
                    public void call(Object... args) {
                        System.out.print(args);
                    }
                });

            }
        });


        socket.on("new message", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        Toast.makeText(ChatBoxActivity.this, data.toString(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        socket.on("me", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        try {
                            String msg=data.getString("msg");
                            String nick=data.getString("nick");

                            StringBuilder messageBuilder=new StringBuilder();
                            messageBuilder.append(nick);
                            messageBuilder.append(":");
                            messageBuilder.append(msg);
                            textMessage.append(messageBuilder+"\n");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(ChatBoxActivity.this, data.toString(), Toast.LENGTH_SHORT).show();
                    }
                });


            }
        });

        socket.on("whisper", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        try {
                            String msg=data.getString("msg");
                            String nick=data.getString("nick");

                            StringBuilder messageBuilder=new StringBuilder();
                            messageBuilder.append(nick);
                            messageBuilder.append(":");
                            messageBuilder.append(msg);
                            textMessage.append(messageBuilder+"\n");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
}
