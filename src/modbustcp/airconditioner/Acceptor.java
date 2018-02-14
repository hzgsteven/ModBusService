package modbustcp.airconditioner;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Acceptor implements Runnable {
	private final ServerSocketChannel ssc;
	private final Selector selector;
	private final String listenIp;

	public Acceptor(Selector selector, ServerSocketChannel ssc, String listenIp) {
		this.ssc = ssc;
		this.selector = selector;
		this.listenIp = listenIp;
	}

	@Override
	public void run() {
		SocketChannel sc;
		try {
			sc = ssc.accept();	// 接受client的连接请求
			System.out.println(sc.socket().getRemoteSocketAddress().toString()+" 已连接");
			
			if (sc != null) {
				sc.configureBlocking(false);
				SelectionKey sk = sc.register(selector, SelectionKey.OP_READ);
				
				//selector.wakeup();	// 使一个阻塞住的selector操作立即返回
				sk.attach(new TCPHandler(sk, sc, listenIp));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
