package de.isento.daimon;

import java.io.IOException;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

public class WebiteTester {

	public static void main(String[] args) {
		try {
			String url = "http://spiegel.de";
			Response response = Jsoup.connect(url).timeout(10000).execute();
			System.out.println("HTTP-Code: " + response.statusCode());

			response = Jsoup.connect(url).followRedirects(false).execute();
			System.out.println(response.header("location"));
			System.out.println("HTTP-Code: " + response.statusCode());
			
			response = Jsoup.connect(url).followRedirects(true).execute();
			System.out.println(response.url());
			System.out.println("HTTP-Code: " + response.statusCode());

			System.out.println("Response.getcontenttype: " + response.contentType());
			
			// *****************
			
			System.out.println(response.header("Content-Language"));
			System.out.println("header contenttype: " + response.header("Content-Type"));
			System.out.println(response.header("Location"));
			//Server Apache/1.3.27 (Unix) (Red-Hat/Linux)
			System.out.println(response.header("Server")); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
