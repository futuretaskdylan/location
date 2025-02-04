package com.sfmap.api.services.core;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

public class HttpTool {
	public static String TAG = HttpTool.class.getSimpleName();
	public static final int CONN_TIMEOUT = 10 * 1000;
	public static final int READ_TIMEOUT = 20 * 1000;
	public static final int MaxTry = 1;
	public static final int TimeoutSeconds = 5;
	public static final int WaitSeconds = 2;

	public static HttpURLConnection makeGetRequest(String path, Proxy proxy)
			throws SearchException {
		if (path == null) {
			// return null;
			throw new SearchException(SearchException.ERROR_INVALID_PARAMETER);
		}
		HttpURLConnection conn = null;
		try {
			URL url = new URL(path);

			if (proxy != null) {
				conn = (HttpURLConnection) url.openConnection(proxy);
			} else {
				conn = (HttpURLConnection) url.openConnection();
			}
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(CONN_TIMEOUT);
			conn.setReadTimeout(READ_TIMEOUT);
			conn.connect();
			Log.d(TAG,"ResponseCode:"+conn.getResponseCode());
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				Log.d(TAG,"SearchException.ERROR_CONNECTION");
				throw new SearchException(SearchException.ERROR_CONNECTION);
			}
		} catch (UnknownHostException e) {
			disConnect(conn);
			throw new SearchException(SearchException.ERROR_UNKNOW_HOST);
		} catch (MalformedURLException e) {
			disConnect(conn);
			throw new SearchException(SearchException.ERROR_URL);
		} catch (ProtocolException e) {
			disConnect(conn);
			throw new SearchException(SearchException.ERROR_PROTOCOL);
		} catch (SocketTimeoutException e) {
			disConnect(conn);

			throw new SearchException(SearchException.ERROR_SOCKE_TIME_OUT);
		} catch (IOException e) {
			disConnect(conn);

			throw new SearchException(SearchException.ERROR_IO);
		}
		return conn;
	}

	private static void disConnect(HttpURLConnection conn) {
		if (conn != null) {
			conn.disconnect();
			conn = null;
		}
	}

	public static HttpURLConnection makePostRequest(String path,
			byte[] entityBytes, Proxy proxy) throws SearchException {
		if (path == null) {
			// return null;
			throw new SearchException(SearchException.ERROR_INVALID_PARAMETER);
		}
		HttpURLConnection conn = null;
		try {
			URL url = new URL(path);
			if (proxy != null) {
				conn = (HttpURLConnection) url.openConnection(proxy);
			} else {
				conn = (HttpURLConnection) url.openConnection();
			}
			conn.setRequestMethod("POST");
			conn.setInstanceFollowRedirects(true);
			conn.setConnectTimeout(CONN_TIMEOUT);
			conn.setReadTimeout(READ_TIMEOUT);
			conn.setDoInput(true);
			conn.setDoOutput(true);// 如果通过post提交数据，必须设置允许对外输出数据
			conn.setUseCaches(false);
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			conn.setRequestProperty("Content-Length",
					String.valueOf(entityBytes.length));
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.connect();
			OutputStream outStream = conn.getOutputStream();
			outStream.write(entityBytes);
			outStream.flush();
			outStream.close();
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new SearchException(SearchException.ERROR_CONNECTION);
			}
		} catch (UnknownHostException e) {
			disConnect(conn);
			throw new SearchException(SearchException.ERROR_UNKNOW_HOST);
		} catch (MalformedURLException e) {
			disConnect(conn);
			throw new SearchException(SearchException.ERROR_URL);
		} catch (ProtocolException e) {
			disConnect(conn);
			throw new SearchException(SearchException.ERROR_PROTOCOL);
		} catch (SocketTimeoutException e) {
			disConnect(conn);
			throw new SearchException(SearchException.ERROR_SOCKE_TIME_OUT);
		} catch (IOException e) {
			disConnect(conn);
			throw new SearchException(SearchException.ERROR_IO);
		}
		return conn;
	}

	// 读取数据
	public static byte[] readStream(InputStream inStream)
			throws SearchException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		try {
			byte[] buffer = new byte[1024];
			int length = -1;
			while ((length = (inStream.read(buffer))) != -1) {
				outStream.write(buffer, 0, length);
				outStream.flush();
			}
		} catch (IOException e) {
			throw new SearchException(SearchException.ERROR_IO);
		} finally {
			if (outStream != null) {
				try {
					outStream.close();
				} catch (IOException e) {
					throw new SearchException(SearchException.ERROR_IO);
				}
			}
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					throw new SearchException(SearchException.ERROR_IO);
				}
			}
		}
		return outStream.toByteArray();
	}

	/**
	 * 将byte数组转化为32位整数
	 * 
	 * @param bytes
	 * @return
	 * @author weiping.liu
	 * @date 2014年1月21日
	 */
	public static int bytesToInt4uint32(byte[] bytes) {
		int addr = bytes[3] & 0xFF;
		addr |= ((bytes[2] << 8) & 0xFF00);
		addr |= ((bytes[1] << 16) & 0xFF0000);
		addr |= ((bytes[0] << 24) & 0xFF000000);
		return addr;
	}

	// 将int整形转化成byte数组
	public static byte[] intToByte4uint32(int i) {
		byte[] abytes = new byte[4];
		abytes[3] = (byte) (0xff & i);
		abytes[2] = (byte) ((0xff00 & i) >> 8);
		abytes[1] = (byte) ((0xff0000 & i) >> 16);
		abytes[0] = (byte) ((0xff000000 & i) >> 24);
		return abytes;
	}

	/**
	 * @功能: 将一个长度为2 byte数组转为short
	 * @参数: byte[] bytesShort要转的字节数组
	 * @返回值: short sRet 转后的short值
	 */
	public static short bytesToShort(byte[] bytesShort) {
		short sRet = 0;
		sRet += (bytesShort[0] & 0xFF) << 8;
		sRet += bytesShort[1] & 0xFF;
		return sRet;
	}

	/**
	 * @功能: 将一个short值转为byte数组
	 * @参数: short sNum 要转的short值
	 * @返回值: byte[] bytesRet 转后的byte数组
	 */
	public static byte[] shortToBytes(short sNum) {
		byte[] bytesRet = new byte[2];
		bytesRet[0] = (byte) ((sNum >> 8) & 0xFF);
		bytesRet[1] = (byte) (sNum & 0xFF);
		return bytesRet;
	}

	public static byte[] intToByteArray(int i) throws IOException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(buf);
		dos.writeInt(i);
		byte[] b = buf.toByteArray();
		dos.close();
		buf.close();
		return b;
	}

	public static int ByteArrayToInt(byte b[]) throws IOException {
		ByteArrayInputStream buf = new ByteArrayInputStream(b);
		DataInputStream dis = new DataInputStream(buf);
		return dis.readInt();
	}
}
