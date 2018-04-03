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
	 * 监听的端口与空调的ip对应
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
		// 1. 打开通道和多路开关
		serverSocketChannel = ServerSocketChannel.open();
		selector = Selector.open();
		
		// 2. ServerSocketChannel中的socket绑定指定的端口号，并设置为非阻塞模式
		serverSocketChannel.socket().bind(new InetSocketAddress(port));
		serverSocketChannel.configureBlocking(false);
		
		// 3. 向selector注册该channel
		SelectionKey sk = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		

		String ip = map.get(port);
		// 4. 利用selectionKey的attach功能绑定Acceptor   如果有事件，触发Acceptor
		sk.attach(new Acceptor(selector, serverSocketChannel, ip));
		System.out.println(ip+":"+port+"监听端口已启动");
		
		this.currentPort = port;
	}

	@Override
	public void run() {
		while (!Thread.interrupted()) { // 作用在当前线程, 在线程被中断前持续运行  
			System.out.println("当前端口正在监听的端口号是" + this.currentPort);  
            System.out.println("Waiting for new event on port: " + serverSocketChannel.socket().getLocalPort() + "...");  
            try {  
                if (selector.select() == 0) {
                    continue;  // 若没有事件就绪则不往下执行  
                }
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
            Set<SelectionKey> selectedKeys = selector.selectedKeys(); // 取得所有已就绪事件的key集合  
            Iterator<SelectionKey> it = selectedKeys.iterator();  
            while (it.hasNext()) {  
                dispatch((SelectionKey) (it.next())); // 根据事件的key进行调度  
                it.remove();  
            }  
        }
	}

	/**
	 * 
	 * @Description: 事件派发 
	 * 
	 * @author hzg
	 * 
	 * @Note
	 * <br><b>Date:</b> 2018年1月5日 下午4:59:33
	 *
	 * @param 
	 * @param key
	 */
	private void dispatch(SelectionKey key) {
		Runnable r = (Runnable) (key.attachment()); // 根据事件之key绑定的对象开新线程(Acceptor)
		if (r != null) {
			r.run();
		}
	}
}
