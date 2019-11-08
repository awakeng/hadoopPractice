package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class API {
	public static void getData(String PATH) throws SQLException, IOException, Exception{
		File file = new File(PATH+"hello.txt");
		FileWriter fw = new FileWriter(file, false);
		
		BufferedReader br = null;
		JSONParser parser = new JSONParser();
		JSONObject obj = null;

//		http://openapi.seoul.go.kr:8088/(인증키)/xml/GetParkInfo/1/5/ 
		URL url = new URL("http://openapi.seoul.go.kr:8088/785965467a7368653637636a796854/json/GetParkInfo/1/100/");
		
		HttpURLConnection urlconnection = (HttpURLConnection) url.openConnection();
		urlconnection.setRequestMethod("GET");
		br = new BufferedReader(new InputStreamReader(urlconnection.getInputStream(), "UTF-8"));
		
		obj = (JSONObject) parser.parse(br.readLine());
		JSONObject obj2 = (JSONObject) obj.get("GetParkInfo");
		long dataNum = (long) obj2.get("list_total_count");
		
		
		JSONArray obj3 = (JSONArray) obj2.get("row");

		ArrayList<String> check = new ArrayList<>();
		for(int i = 0 ; i < obj3.size(); i++) {
			JSONObject j =(JSONObject) obj3.get(i);
			if(!j.get("QUE_STATUS_NM").equals("미연계중")) {
				String name = (String) j.get("PARKING_NAME");
				double total = Double.parseDouble(j.get("CAPACITY").toString());
				double available = total - Double.parseDouble(j.get("CUR_PARKING").toString());
				String time = j.get("CUR_PARKING_TIME").toString();
				String lat = j.get("LAT").toString();
				String lng = j.get("LNG").toString();
				String address = (String) j.get("ADDR");
				if(!check.contains(name)) {
					check.add(name);
					fw.write(name+"\t"+total+"\t"+available+"\t"+time+"\t"+lat+"\t"+lng+"\t"+address+"\n");
				}
			}
			
		}
		fw.close();
	}
	

	public static void main(String[] args) throws Exception {
		final String PATH = args[0];
		getData(PATH);
	}
}
