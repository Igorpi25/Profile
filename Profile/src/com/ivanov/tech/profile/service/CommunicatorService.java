package com.ivanov.tech.profile.service;

import java.util.ArrayList;

import com.ivanov.tech.communicator.service.TransportBase;

public class CommunicatorService extends com.ivanov.tech.communicator.service.CommunicatorService{

	@Override
	public ArrayList<TransportBase> createTransports() {
		
		ArrayList<TransportBase> transports=new ArrayList<TransportBase>();		
		
		TransportProfile transportprofile=new TransportProfile(this);		
		transports.add(transportprofile);
		
		return transports;
	}

}
