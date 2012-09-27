package com.zdonnell.eve.api;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class APIObject {

	protected APICredentials credentials;

	protected void setCredentials(APICredentials credentials) {
		this.credentials = credentials;
	}
}
