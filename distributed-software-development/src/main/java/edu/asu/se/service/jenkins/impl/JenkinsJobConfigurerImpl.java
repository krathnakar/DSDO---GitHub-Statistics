package edu.asu.se.service.jenkins.impl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.asu.se.service.jenkins.GroovyScriptGenerator;
import edu.asu.se.service.jenkins.JenkinsJobConfigurer;

@Service
public class JenkinsJobConfigurerImpl implements JenkinsJobConfigurer {

	@Autowired
	GroovyScriptGenerator scriptGenerator;

	@Override
	public Boolean setupJob(String projectName, String branchName, String rootPOMLoc) {

		Boolean result = false;

		Boolean scriptCreation = scriptGenerator.generateScipt(projectName, branchName, rootPOMLoc);

		if (scriptCreation != Boolean.TRUE) {
			result = scriptCreation;
		} else {
			result = buildDSLJob(projectName, branchName);
		}

		return result;
	}

	private Boolean buildDSLJob(String projectName, String branchName) {

		try {
			String url = "http://localhost:8080/jenkins/job/DSL%20Git%20projects%20builder/build";
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			BufferedReader br = null;
			int responseCode = 0;
			// optional default is GET
			con.setRequestMethod("GET");

			responseCode = con.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);

			url = "http://localhost:8080/jenkins/job/DSL%20Git%20projects%20builder/lastBuild/api/json";
			obj = new URL(url);
			con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");

			responseCode = con.getResponseCode();

			if (responseCode >= 200 && responseCode < 300) {
				Date date = new Date();
				Date newDate = new Date();
				url = "http://localhost:8080/jenkins/job/" + projectName.replace("/", "-") + "-" + branchName
						+ "/lastBuild/api/json";

				while (true) {
					try {
						obj = new URL(url);
						con = (HttpURLConnection) obj.openConnection();
						con.setRequestMethod("GET");
						responseCode = con.getResponseCode();
						br = new BufferedReader(new InputStreamReader(con.getInputStream()));
						String test = br.readLine();
						if (br != null && test.contains("\"result\":null")) {
							con.disconnect();
							continue;
						} else {
							break;
						}

					} catch (FileNotFoundException e) {
						newDate = new Date();
						long minutes = (newDate.getTime() - date.getTime()) / (60 * 1000) % 60;
						if (minutes > 10)
							break;
						continue;
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

}
