package modbustcp.airconditioner;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class TCPReactor implements Runnable {
	private final ServerSocketChannel serverSocketChannel;
	private final Selector selector;
	private static Map<Integer, String> map = new HashMap<Integer, String>();
	private int currentPort;

	/**
	 * �����Ķ˿���յ���ip��Ӧ
	 */
	static {
		map.put(502, "192.168.218.21");
		map.put(503, "192.168.218.23");
		map.put(504, "192.168.218.26");
		map.put(505, "192.168.218.27");
		map.put(506, "192.168.218.28");
		map.put(507, "192.168.218.29");
		map.put(508, "192.168.218.30");
		map.put(509, "192.168.218.31");
		map.put(510, "192.168.218.32");
		map.put(511, "192.168.218.33");
		map.put(512, "192.168.218.34");
		map.put(513, "192.168.218.35");
		map.put(514, "192.168.218.36");
		map.put(515, "192.168.218.37");
	}

	public TCPReactor(int port) throws IOException {
		// 1. ��ͨ���Ͷ�·����
		serverSocketChannel = ServerSocketChannel.open();
		selector = Selector.open();
		
		// 2. ServerSocketChannel�е�socket��ָ���Ķ˿ںţ�������Ϊ������ģʽ
		serverSocketChannel.socket().bind(new InetSocketAddress(port));
		serverSocketChannel.configureBlocking(false);
		
		// 3. ��selectorע���channel
		SelectionKey sk = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		

		String ip = map.get(port);
		// 4. ����selectionKey��attach���ܰ�Acceptor   ������¼�������Acceptor
		sk.attach(new Acceptor(selector, serverSocketChannel, ip));
		System.out.println(ip+":"+port+"�����˿�������");
		
		this.currentPort = port;
	}

	@Override
	public void run() {
		while (!Thread.interrupted()) { // �����ڵ�ǰ�߳�, ���̱߳��ж�ǰ��������  
			System.out.println("��ǰ�˿����ڼ����Ķ˿ں���" + this.currentPort);  
            System.out.println("Waiting for new event on port: " + serverSocketChannel.socket().getLocalPort() + "...");  
            try {  
                if (selector.select() == 0) {
                    continue;  // ��û���¼�����������ִ��  
                }
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
            Set<SelectionKey> selectedKeys = selector.selectedKeys(); // ȡ�������Ѿ����¼���key����  
            Iterator<SelectionKey> it = selectedKeys.iterator();  
            while (it.hasNext()) {  
                dispatch((SelectionKey) (it.next())); // �����¼���key���е���  
                it.remove();  
            }  
        }
	}

	/**
	 * 
	 * @Description: �¼��ɷ� 
	 * 
	 * @author hzg
	 * 
	 * @Note
	 * <br><b>Date:</b> 2018��1��5�� ����4:59:33
	 *
	 * @param 
	 * @param key
	 */
	private void dispatch(SelectionKey key) {
		Runnable r = (Runnable) (key.attachment()); // �����¼�֮key�󶨵Ķ������߳�(Acceptor)
		if (r != null) {
			r.run();
		}
	}
}
