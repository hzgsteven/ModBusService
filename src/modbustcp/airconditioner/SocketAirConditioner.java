package modbustcp.airconditioner;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
	// һ�����̹߳�����socket
	public static void main(String[] args) {
		ExecutorService executorService = Executors.newFixedThreadPool(14);
		
        for(int i=0;i<14;i++){
        	System.out.println("�˿�"+(502+i)+"��������");
        	SocketAirConditioner myTask = new SocketAirConditioner(502+i);	// ����14���߳�
        	executorService.execute(myTask);
        }
        executorService.shutdown();  // ��ֹ�������ύ�����Executor
	}

	
}
