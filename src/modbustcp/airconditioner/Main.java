package modbustcp.airconditioner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

	// һ�����̹߳�����socket
	public static void main(String[] args) {
		ExecutorService executorService = Executors.newFixedThreadPool(14);
		
        for(int i=0;i<14;i++){
        	System.out.println("�˿�"+(502+i)+"��������");
        	SocketAirConditioner conditions = new SocketAirConditioner(502+i);	// ����14���̼߳���14���˿�
        	executorService.execute(conditions);
        }
        executorService.shutdown();  // ��ֹ�������ύ�����Executor
	}

}
