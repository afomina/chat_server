import java.io.IOException;
import net.Server;

public class Main {

	public static void main(String[] args) {
		try {
			Server server = new Server();
			server.listen();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
