import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Monitor extends Thread {
    ServerSocket serverSocket;
    ArrayList<Message> messages;
    int total_count;
    Map<String, Long> sync_times;

    public Monitor(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        messages = new ArrayList<Message>();
        total_count = 0;
        sync_times = new HashMap<String, Long>();
    }

    public void print_msgs_sorted() {
        Collections.sort(messages, new Comparator<Message>() {
            @Override
            public int compare(Message lhs, Message rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return lhs.getTime() > rhs.getTime() ? -1 : (lhs.getTime() < rhs.getTime()) ? 1 : 0;
            }
        });
        for(int i = 0; i < messages.size(); i++) {
            System.out.println("Message: " + messages.get(i).getText() + ", Time: " + messages.get(i).getTime());
        }
    }

    public void print_sync_times() {
        for(Map.Entry<String, Long> entry : sync_times.entrySet()) {
            String ip = entry.getKey();
            Long time = entry.getValue();
            System.out.println("IP: " + ip + ", Sync time: " + time);
        }
    }

    public void print() {
        print_msgs_sorted();
        System.out.println("Total number of messages for synchronization: " + total_count);
        print_sync_times();
    }

    public void run() {
        try {
            while (true) {
                Socket connectionSocket = serverSocket.accept();
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                String clientSentence = inFromClient.readLine();
                if(clientSentence.equals("time")) {
                    clientSentence = inFromClient.readLine();
                    long time = Long.valueOf(clientSentence);
                    clientSentence = inFromClient.readLine();
                    if(clientSentence.equals("message")) {
                        String msg = inFromClient.readLine();
                        Message message = new Message(time, msg);
                        messages.add(message);
                    }
                }
                else if(clientSentence.equals("count")) {
                    int count = Integer.valueOf(inFromClient.readLine());
                    total_count += count;
                }
                else if(clientSentence.equals("sync")) {
                    String ip = (((InetSocketAddress) connectionSocket.getRemoteSocketAddress()).getAddress()).toString().replace("/","");
                    long time = Long.valueOf(inFromClient.readLine());
                    sync_times.put(ip, time);
                }
                connectionSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
