package cn.sgz.loadblance;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ZkServerClient {
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

	// 获取所有的服务地址
	public static List<String> listServer = new ArrayList<String>();

	// 服务调用次数
	private static int count = 1;
	// 会员服务集群数量，实际开发中不要写死，
	private static int memberServerCount = 1;


	public static void main(String[] args) {
		initServer();
		ZkServerClient client = new ZkServerClient();
		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			String name;
			try {
				name = console.readLine();
				if ("exit".equals(name)) {
					System.exit(0);
				}
				client.send(name);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	//初始化所有server
	public static void initServer() {
		listServer.clear();
		// 从zk获取最新获取的注册服务连接
		ZkClient zkClient = new ZkClient(new ZkConnection(CONNECT_ADDR, SESSION_OUTTIME));
		// 获当前下子节点
		List<String> children = zkClient.getChildren(memberServerPath);
		for (String path : children) {
			// 读取子节点value值
			listServer.add((String) zkClient.readData(memberServerPath + "/" + path));
		}
		memberServerCount = listServer.size();//计算服务器数量
		System.out.println("最新服务信息listServer:" + listServer.toString()+"====memberServerCount==="+memberServerCount);
		// 订阅子节点事件
		zkClient.subscribeChildChanges(memberServerPath, new IZkChildListener() {
			// 子节点发生变化
			public void handleChildChange(String parentPath, List<String> childrens) throws Exception {
				listServer.clear();
				for (String subP : childrens) {
					// 读取子节点value值
					listServer.add((String) zkClient.readData(memberServerPath + "/" + subP));
				}
				memberServerCount = listServer.size();//计算服务器数量
				System.out.println("节点发生变化，重新获取最新服务信息listServer:" + listServer.toString()+"====memberServerCount==="+memberServerCount);
			}
		});
	}



	// 获取当前server信息
	public static String getServer() {
		System.out.println("====memberServerCount==="+memberServerCount);
		//这里使用轮询算法（取模运算）
		String serverName = listServer.get(count % memberServerCount);//获取要执行的服务器
		++count;
		return serverName;
	}

	public void send(String name) {
		String server = ZkServerClient.getServer();
		String[] cfg = server.split(":");
		Socket socket = null;
		BufferedReader in = null;
		PrintWriter out = null;
		try {
			socket = new Socket(cfg[0], Integer.parseInt(cfg[1]));
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);

			out.println(name);
			while (true) {
				String resp = in.readLine();
				if (resp == null)
					break;
				else if (resp.length() > 0) {
					System.out.println("Receive : " + resp);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				out.close();
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
