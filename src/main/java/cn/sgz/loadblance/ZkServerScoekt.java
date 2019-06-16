package cn.sgz.loadblance;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

//##ServerScoekt服务端
public class ZkServerScoekt implements Runnable {

	/**
	 * 集群连接地址
	 */
	private static final String CONNECT_ADDR = "192.168.184.131:2181,192.168.184.132:2181,192.168.184.133:2181";
	/**
	 * session超时时间(心跳检测时间周期)
	 */
	private static final int SESSION_OUTTIME = 2000;
	/**
	 * 服务父节点路径
	 */
	private static final String memberServerPath = "/member";

	private int port = 18081;

	public static void main(String[] args) throws IOException {
		int port = 18081;
		ZkServerScoekt server = new ZkServerScoekt(port);
		Thread thread = new Thread(server);
		thread.start();
	}

	public ZkServerScoekt(int port) {
		this.port = port;
	}

	// 将服务信息注册到注册中心上去
	public void regServer() {
		ZkClient zkClient = new ZkClient(new ZkConnection(CONNECT_ADDR, SESSION_OUTTIME));
		if (!zkClient.exists(memberServerPath)) {//创建/member节点
			zkClient.createPersistent(memberServerPath);
		}
		String path = "/member/server-" + port;
		if (zkClient.exists(path)) {
			zkClient.delete(path);
		}
		String value="127.0.0.1:" + port;
		zkClient.createEphemeral(path, "127.0.0.1:" + port);
		System.out.println("##服务注册成功###"+value);
	}

	public void run() {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
			regServer();
			System.out.println("Server start port:" + port);
			Socket socket = null;
			while (true) {
				socket = serverSocket.accept();
				new Thread(new ServerHandler(socket)).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (serverSocket != null) {
					serverSocket.close();
				}
			} catch (Exception e2) {

			}
		}
	}

}
