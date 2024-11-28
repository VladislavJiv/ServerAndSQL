import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client2 {
    public static void main(String[] args) throws IOException, InterruptedException {
        DataInputStream in = new DataInputStream(System.in);
        DataOutputStream out = new DataOutputStream(System.out);
        Socket socket = new Socket("localhost", 5555);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        while (true) {
            out.writeUTF("Hello I'm Client2");
            System.out.println(in.readUTF());
            Thread.sleep(2000);

        }
    }
}
