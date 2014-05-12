/* 
************************************************************************

# RESTful API for Zabbix based on Java.
#
# © Copyright 2014 Yahya Al-Hazmi, TU Berlin.
# Authors: Mingyuan Wu and Yahya Al-Hazmi
# Licensed under the Apache License, Version 2.0 (the "License"); 
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0 
#       
# Unless required by applicable law or agreed to in writing, software 
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License
#
# This API allows you to request monitoring information from Zabbix-Server 
# through Zabbix-API

 ************************************************************************

 This class is aimed to achieve the goal of getting memory information 
 from zabbix.
 
 ************************************************************************
*/


import java.util.*;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import java.util.Properties;
import java.io.FileInputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PutMethod;

@Controller
public class MemoryController{
//---------------This Part starts the Zabbix connection---------------

    @RequestMapping("/memory")
    public @ResponseBody Memory memory(
		@RequestParam(value="authentication", required=false, defaultValue="Error") String authentication, @RequestParam(value="hostid", required=false, defaultValue="") String hostid, @RequestParam(value="metricType", required=false, defaultValue="totalMemory" ) String name) throws  FileNotFoundException, UnsupportedEncodingException, IOException{

    		Properties props = new Properties();
//    		FileInputStream fis = new FileInputStream("/home/ubuntu/gs-rest-service/initial/src/main/java/tubav/properties.xml");
    		FileInputStream fis = new FileInputStream("properties.xml");
    		//loading properites from properties file
    		props.loadFromXML(fis);

		String server_ip =  props.getProperty("server_ip");
    		String ZABBIX_API_URL = "http://" + server_ip + "/api_jsonrpc.php"; // 1.2.3.4 is your zabbix_server_ip

		JSONParser parser=new JSONParser();
		HttpClient client = new HttpClient();
		
		PutMethod putMethod = new PutMethod(ZABBIX_API_URL);
		putMethod.setRequestHeader("Content-Type", "application/json-rpc"); // content-type is controlled in api_jsonrpc.php, so set it like this
		
		// create json object for apiinfo.version 
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("jsonrpc","2.0");
		jsonObj.put("method","item.get");
		JSONObject params = new JSONObject();
		params.put("output","extend");
		params.put("hostid",hostid);
		JSONObject search = new JSONObject();
		search.put("key_","memory");
		params.put("search", search);
		params.put("sortfield","name");	
		jsonObj.put("params",params);
		jsonObj.put("auth", authentication);// todo
		jsonObj.put("id",new Integer(1));
		
		putMethod.setRequestBody(jsonObj.toString()); // put the json object as input stream into request body 
		
		String loginResponse = "";
		String memory= "";
		String clock="";
		String metricType="";
		
		try {
			client.executeMethod(putMethod); // send to request to the zabbix api
			
			loginResponse = putMethod.getResponseBodyAsString(); // read the result of the response

                	Object obj = parser.parse(loginResponse);
                	JSONObject obj2 = (JSONObject) obj;
                	String jsonrpc= (String) obj2.get("jsonrpc");
                	JSONArray array = (JSONArray) obj2.get("result");

	                for (int i = 0; i < array.size(); i++){
       		                JSONObject tobj = (JSONObject) array.get(i);
	//			lastValue = getLastValue(tobj);
	//			lastClock = getLastClock(tobj);
				if (!tobj.get("hostid").equals(hostid)) continue;
				if (name.equals("totalMemory") && tobj.get("name").equals("Total memory") ){
					memory = (String) tobj.get("lastvalue");
					clock = (String) tobj.get("lastclock");	
					metricType="Total Memeory";
					return new Memory(hostid, metricType, memory, clock);	
				} 
				else if (name.equals("cachedMemory") && tobj.get("name").equals("Cached memory") ){
					memory = (String) tobj.get("lastvalue");
					clock = (String) tobj.get("lastclock");
					metricType="Cached Memory";
					return new Memory(hostid, metricType, memory, clock);	
				}
				else if (name.equals("freeMemory") && tobj.get("name").equals("Free memory")){
					memory = (String) tobj.get("lastvalue");
					clock = (String) tobj.get("lastclock");
					metricType = "Free Memory";
					return new Memory(hostid, metricType, memory, clock);	
				}
				else if (name.equals("bufferedMemory") && tobj.get("name").equals("Buffers memory")){
					memory = (String) tobj.get("lastvalue");
					clock = (String) tobj.get("lastclock");
					metricType = "Buffered Memory";
					return new Memory(hostid, metricType, memory, clock);	
				}
				else if (name.equals("sharedMemory") && tobj.get("name").equals("Shared memory")){
					memory = (String) tobj.get("lastvalue");
					clock = (String) tobj.get("lastclock");
					metricType = "Shared Memory";
					return new Memory(hostid, metricType, memory, clock);	
				}	
				else {
					continue;
				}
                	}
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch(ParseException pe){
	                System.out.println("Error");
        	}

		return new Memory(hostid, metricType, memory, clock);	
	}
//--------------This part ends the Zabbix connection------------------
}
