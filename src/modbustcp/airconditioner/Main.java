package modbustcp.airconditioner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

	// 一个主线程管理多个socket
	public static void main(String[] args) {
		ExecutorService executorService = Executors.newFixedThreadPool(14);
		
        for(int i=0;i<14;i++){
        	System.out.println("端口"+(502+i)+"正在启动");
        	SocketAirConditioner conditions = new SocketAirConditioner(502+i);	// 启动14条线程监听14个端口
        	executorService.execute(conditions);
        }
        executorService.shutdown();  // 防止新任务提交给这个Executor
	}

}
