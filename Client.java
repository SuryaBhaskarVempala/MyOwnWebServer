import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    public void run() throws IOException {
        int port  = 8010;
        InetAddress address = InetAddress.getByName("localhost");
        Socket socket = new Socket(address,port);

        PrintWriter toServer = new PrintWriter(socket.getOutputStream());
        BufferedReader fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String line = fromServer.readLine();

        System.out.println(line);

        toServer.close();
        fromServer.close();
    }
    public static  void  main(String args[]) throws IOException {
        Client client = new Client();
        client.run();
    }
}
