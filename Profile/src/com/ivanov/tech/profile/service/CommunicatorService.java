package com.ivanov.tech.profile.service;

import java.util.ArrayList;

import com.ivanov.tech.communicator.service.TransportBase;

public class CommunicatorService extends com.ivanov.tech.communicator.service.CommunicatorService{
	
	//---------------Communicator Demo URL-------------
    public final static String URL_PROTOCOL="ws://";
    public final static String URL_DOMEN="igorpi25.ru";  
    public final static String URL_PORT=":8001";//Websocket server port
    public final static String URL_SERVER=URL_PROTOCOL+URL_DOMEN+URL_PORT;
    
	@Override
	public ArrayList<TransportBase> createTransports() {
		
		ArrayList<TransportBase> transports=new ArrayList<TransportBase>();		
		
		TransportProfile transportprofile=new TransportProfile(this);		
		transports.add(transportprofile);
		
		return transports;
	}

	@Override
	public String getServerUrl() {
		return URL_SERVER;
	}
	
	

}
