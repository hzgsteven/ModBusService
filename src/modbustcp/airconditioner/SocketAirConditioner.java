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
	// 一个主线程管理多个socket
	public static void main(String[] args) {
		ExecutorService executorService = Executors.newFixedThreadPool(14);
		
        for(int i=0;i<14;i++){
        	System.out.println("端口"+(502+i)+"正在启动");
        	SocketAirConditioner myTask = new SocketAirConditioner(502+i);	// 启动14条线程
        	executorService.execute(myTask);
        }
        executorService.shutdown();  // 防止新任务提交给这个Executor
	}

	
}
