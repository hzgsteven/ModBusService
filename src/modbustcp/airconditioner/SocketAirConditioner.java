package modbustcp.airconditioner;

import java.io.IOException;

/**
 * 空调的网络设备
 * Description: 
 * 
 * @author hzg
 * @Date   上午10:07:52
 *
 */
public class SocketAirConditioner implements Runnable {
	private int port;
    public SocketAirConditioner(int port) {
        this.port = port;
    }
 
    @Override
    public void run() {
    	try {  
            TCPReactor reactor = new TCPReactor(this.port);  
            reactor.run();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }
    }
	
}
