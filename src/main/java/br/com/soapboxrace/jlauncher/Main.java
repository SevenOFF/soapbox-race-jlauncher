package br.com.soapboxrace.jlauncher;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import br.com.soapboxrace.jlauncher.swing.MainWindow;
import br.com.soapboxrace.jlauncher.util.ConfigDao;
import br.com.soapboxrace.jlauncher.util.CopyFiles;
import br.com.soapboxrace.jlauncher.util.EmailValidator;
import br.com.soapboxrace.jlauncher.util.HttpRequest;
import br.com.soapboxrace.jlauncher.util.Md5Files;
import br.com.soapboxrace.jlauncher.vo.ConfigVO;
import br.com.soapboxrace.jlauncher.vo.RequestVO;
import br.com.soapboxrace.jlauncher.vo.UserVO;

public class Main {

	public static ConfigDao configDao = new ConfigDao();

	public static void main(String[] args) {
		new MainWindow().setVisible(true);
	}

	public static ConfigVO loadConfig() {
		return configDao.getConfig();
	}

	public static LoginCreate login(String url, String email, String password, boolean saveCredentials) {
		ConfigVO configVO = loadConfig();
		configVO.setServerURL("Latest server;" + url);
		configDao.saveConfig(configVO);
		UserVO userVO = new UserVO(email, password);
		if ("********************".equals(password)) {
			userVO.setShaPassword(configVO.getPasswordSHA1());
		}
		if (saveCredentials) {
			saveCredentials(email, userVO.getShaPassword());
		} else {
			emptyCredentials();
		}
		LoginCreate login = new LoginCreate(url, userVO);
		return login;
	}

	public static void saveGamePath(String gameExePath) {
		ConfigVO configVO = loadConfig();
		configVO.setGameExePath(gameExePath);
		configDao.saveConfig(configVO);
	}

	public static void saveCredentials(String email, String passwordSHA1) {
		ConfigVO configVO = loadConfig();
		configVO.setEmail(email);
		configVO.setPasswordSHA1(passwordSHA1);
		configVO.setSaveCredentials(true);
		configDao.saveConfig(configVO);
	}

	public static void emptyCredentials() {
		ConfigVO configVO = loadConfig();
		configVO.setEmail("");
		configVO.setPasswordSHA1("");
		configVO.setSaveCredentials(false);
		configDao.saveConfig(configVO);
	}

	public static void setSaveCredentials(boolean saveCredentials) {
		ConfigVO configVO = loadConfig();
		configVO.setSaveCredentials(saveCredentials);
		configDao.saveConfig(configVO);
	}

	public static LoginCreate create(String url, String email, String password) {
		ConfigVO configVO = loadConfig();
		configVO.setServerURL("Latest server;" + url);
		configDao.saveConfig(configVO);
		UserVO userVO = new UserVO(email, password);
		LoginCreate login = new LoginCreate(url, userVO);
		return login;
	}

	public static boolean checkGameMd5(String filename) {
		return Md5Files.checkExeFile(filename);
	}

	public static boolean checkGameFiles(String url) {
		ConfigVO configVO = configDao.getConfig();
		String gameExePath = configVO.getGameExePath();
		HttpRequest httpRequest = new HttpRequest(url);
		String fileCheckURL = httpRequest.getFileCheckURL();
		RequestVO doRequest = httpRequest.doRequest(fileCheckURL);
		String response = doRequest.getResponse();
		File file = new File(gameExePath);
		String path = file.getParentFile().getPath();
		List<String> list = Arrays.asList(response.split("\\n"));
		for (String string : list) {
			String[] split = string.split(" ");
			String md5Str = split[0];
			String filename = path + "/" + split[1];
			try {
				if (!Md5Files.checkFileHash(md5Str, filename)) {
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	public static boolean copyModules(String path) {
		Path p = Paths.get(path);
		Path folder = p.getParent();
		String pathTo = folder.toString();
		CopyFiles.copyAllFiles(pathTo);
		return Md5Files.checkModules(pathTo);
	}

	public static boolean checkEmail(String email) {
		EmailValidator emailValidator = new EmailValidator(email);
		return emailValidator.isValid();
	}
}
