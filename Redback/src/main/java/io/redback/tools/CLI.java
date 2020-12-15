package io.redback.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Properties;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import io.firebus.Firebus;

public class CLI {

	public static void main(String[] args) {
		try {
			BufferedReader lineReader = new BufferedReader(new InputStreamReader(System.in));
			String token = null;
			String fbnetwork = null;
			String fbpassword = null;
			String objectService = null;
			String username = null;
			String jwtissuer = null;
			String jwtsecret = null;				
			String home = System.getProperty("user.home");
			String fileName = home + "/.redbackcli.properties";
			File propFile = new File(fileName);
			Properties prop = new Properties();
			if(propFile.exists()) {
				prop.load(new FileInputStream(propFile));
				username = prop.getProperty("username");
				jwtsecret = prop.getProperty("jwtsecret");
				jwtissuer = prop.getProperty("jwtissuer");
				fbnetwork = prop.getProperty("fbnet");
				fbpassword = prop.getProperty("fbpass");
				objectService = prop.getProperty("os");
			} else {
				System.out.print("Username: ");
				username = lineReader.readLine();
				prop.put("username", username);
				System.out.print("JWT Secret: ");
				jwtsecret = lineReader.readLine();
				prop.put("jwtsecret", jwtsecret);
				System.out.print("JWT Issuer: ");
				jwtissuer = lineReader.readLine();
				prop.put("jwtissuer", jwtissuer);
				System.out.print("Firebus Network: ");
				fbnetwork = lineReader.readLine();
				prop.put("fbnet", fbnetwork);
				System.out.print("Firebus Password: ");
				fbpassword = lineReader.readLine();
				prop.put("fbpass", fbpassword);
				System.out.print("Object service: ");
				objectService = lineReader.readLine();
				prop.put("os", objectService);
				prop.store(new FileOutputStream(fileName), "");
			}
			Algorithm algorithm = Algorithm.HMAC256(jwtsecret);
			token = JWT.create()
					.withIssuer(jwtissuer)
					.withClaim("email", username)
					.withExpiresAt(new Date(System.currentTimeMillis() + 3600000))
					.sign(algorithm);
			
			if(token != null && fbnetwork != null && fbpassword != null) {
				Firebus firebus = new Firebus(fbnetwork, fbpassword);
				boolean quit = false;
				while(!quit) {
					System.out.print("> ");
					String cmd = lineReader.readLine();
					if(cmd.equals("quit")) {
						quit = true;
					} else if(cmd.startsWith("export ")) {
						String[] parts = cmd.split(" ");
						String set = parts[1];
						String domain = parts[2];
						ExportData export = new ExportData(firebus, token, objectService, set, domain, "export.json");
						export.exportData();
						System.out.println("Export done");
					} else if(cmd.startsWith("import ")) {
						String[] parts = cmd.split(" ");
						String domain = parts[1];
						String filename = parts[2];
						ImportData importData = new ImportData(firebus, token, objectService, domain, filename);
						importData.importData();
						System.out.println("Import done");
					} 
				}
				firebus.close();
			} else {
				System.out.println("Missing configs, delete the properties file and restart");
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
