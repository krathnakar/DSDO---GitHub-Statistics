package edu.asu.se.controller;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import edu.asu.se.git.stats.GitJobs;

@RestController
public class GitAutoUpdateController {

	@Autowired
	GitJobs gitJobs;

	@RequestMapping(value = "/build/{project}", method = RequestMethod.GET)
	public ResponseEntity<String> buildProject(@PathVariable("project") String project) {
		int projectLastIndex = project.lastIndexOf("-");
		String projectName = project.substring(0, projectLastIndex).replaceFirst("-", "/");
		String branchName = project.substring(projectLastIndex + 1, project.length());
		checkBuildStatus(project);
		gitJobs.gitResult(projectName, branchName);
		return new ResponseEntity<String>("Completed", HttpStatus.OK);
	}

	private void checkBuildStatus(String project) {
		Date date = new Date();
		Date newDate = new Date();
		String url = "http://localhost:8080/jenkins/job/" + project + "/lastBuild/api/json";

		while (true) {
			try {
				URL obj = new URL(url);
				obj = new URL(url);
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();
				con.setRequestMethod("GET");
				BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
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
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
