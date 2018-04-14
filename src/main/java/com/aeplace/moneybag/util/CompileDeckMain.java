package com.aeplace.moneybag.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

public class CompileDeckMain {

	public static void main(String[] args) throws URISyntaxException {
		Utility util = new Utility();
		try {
			String path = "C:/Users/Alex/Documents/Github/moneybags/images";
			String encoded = Utility.compileCards(path);
			if (encoded != null) {
				Utility.submitDeck("encounter", encoded);
				System.out.println("success");
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("failure");
		}
	}

}
