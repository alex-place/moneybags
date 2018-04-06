package com.aeplace.moneybag;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.aeplace.moneybag.util.Utility;
import com.aeplace.moneybag.value.EncounterValue;

@Controller
public class MainController {

	@CrossOrigin(origins = "*")
	@GetMapping("/test")
	public String testForm(Model model) throws IOException {
		model.addAttribute("encounterValue", new EncounterValue());
		return "encounterbuilder";
	}

	@CrossOrigin(origins = "*")
	@GetMapping("/encounterbuilder")
	public String greetingForm(Model model) {
		model.addAttribute("encounterValue", new EncounterValue());
		return "encounterbuilder";
	}

	@CrossOrigin(origins = "*")
	@PostMapping("/encounterbuilder")
	public String greetingSubmit(HttpServletRequest request, @ModelAttribute EncounterValue encounterValue)
			throws IOException {

		URL url = new URL(encounterValue.getBackground());
		InputStream in = new BufferedInputStream(url.openStream());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int n = 0;
		while (-1 != (n = in.read(buf))) {
			out.write(buf, 0, n);
		}
		out.close();
		in.close();
		byte[] response = out.toByteArray();

		String tmp = "data:image/png;base64," + Base64.getEncoder().encodeToString(response);

		// //String phyPath =
		// request.getSession().getServletContext().getRealPath("/");
		// String phyPath = "/tmp";
		// String newUrl = phyPath + "/tempimage" + new Random().nextInt(999) +
		// ".jpg";
		// File file = new File(newUrl);
		//
		// FileOutputStream fos = new FileOutputStream(file);
		// fos.write(response);
		// fos.close();

		encounterValue.setBackground(tmp);
		return "encounterbuilder_output";
	}

	@RequestMapping(value = "/encounterbuilder/push", method = RequestMethod.POST)
	public @ResponseBody String processAJAXRequest(@RequestParam("canvas") String canvas) throws IOException {

		String response = "";
		// Process the request
		// Prepare the response string

		try {
			Utility.submitCard(canvas);
			response = "success";
		} catch (IOException e) {
			e.printStackTrace();
			response = "failure";
		}
		return response;
	}

}
