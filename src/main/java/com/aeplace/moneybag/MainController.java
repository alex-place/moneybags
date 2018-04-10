package com.aeplace.moneybag;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.aeplace.moneybag.util.Utility;
import com.aeplace.moneybag.value.EncounterForm;

@Controller
public class MainController {

	@CrossOrigin(origins = "*")
	@GetMapping("/encounterbuilder")
	public String greetingForm(Model model) {
		model.addAttribute("encounterValue", new EncounterForm());
		
		List rarity = new ArrayList(Arrays.asList(new String[]{"Easy Creature", "Medium Creature"}));
		model.addAttribute("rarity", rarity);
		
		List types = new ArrayList(Arrays.asList(new String[]{"Undead", "Beast", "Monstrosity", "Humanoid", "Elemental"}));
		model.addAttribute("type", types);
		
		List prints = new ArrayList(Arrays.asList(new String[]{"1st Ed", "2nd Ed"}));
		model.addAttribute("print", prints);
		
		List collections = new ArrayList(Arrays.asList(new String[]{"TST", "PMB"}));
		model.addAttribute("collection", collections);
		
		List gold = new ArrayList(Arrays.asList(new String[]{"0","1","2","3","4","5","6","7","8","9","10"}));
		model.addAttribute("gold", gold);
		
		return "encounterbuilder";
	}

	@RequestMapping(value = { "/test" }, method = RequestMethod.GET)
	public String selectOptionExample1Page(Model model) {
		EncounterForm encounter = new EncounterForm();
		model.addAttribute("encounter", encounter);
		
//		List<Item> rarity = EncounterSelection.getRarities();
		List rarity = new ArrayList(Arrays.asList(new String[]{"Easy Creature", "Medium Creature"}));
		model.addAttribute("rarity", rarity);
		
		return "test";
	}

	@CrossOrigin(origins = "*")
	@PostMapping("/encounterbuilder")
	public String greetingSubmit(HttpServletRequest request, @Valid EncounterForm encounterValue, BindingResult result)
			throws IOException {

		if(result.hasErrors()){
			return "redirect:/encounterbuilder";
		}
		
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
			canvas = canvas.substring("data:image/png;base64,".length());
			Utility.submitCard(canvas);
			response = "success";
		} catch (IOException e) {
			e.printStackTrace();
			response = "failure";
		}
		return response;
	}

}
