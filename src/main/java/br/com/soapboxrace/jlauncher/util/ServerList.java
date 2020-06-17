package br.com.soapboxrace.jlauncher.util;

import br.com.soapboxrace.jlauncher.vo.RequestVO;

public class ServerList {

	private HttpRequest httpRequest = new HttpRequest("");

	public String[] getServerList() {
		RequestVO doRequest = httpRequest.doRequest("https://raw.githubusercontent.com/SevenOFF/Soapbox-Launcher-Server-list/master/serverlist.txt");
		String serverList[] = null;
		if (doRequest.getResponseCode() == 200) {
			String response = doRequest.getResponse();
			serverList = response.split("\n");
			System.out.println(response);
		}
		return serverList;
	}

}
