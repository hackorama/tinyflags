package com.hackorama.flags;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

/**
 * Minimal version of country flags micro service. Loads the data from data file
 * into memory on start
 * 
 * @author Kishan Thomas (kishan.thomas@gmail.com)
 *
 */
@RestController
@EnableAutoConfiguration
@SpringBootApplication
public class App implements ApplicationListener<ApplicationReadyEvent> {

	private class Continent {
		private String continent;
		private List<Country> countries = new ArrayList<>();
	}

	private class Country {
		private String name;
		private String flag;
	}

	private static org.slf4j.Logger logger = LoggerFactory.getLogger(App.class);

	private static Map<String, String> countryFlagMap = new HashMap<>();
	private static Map<String, Map<String, String>> continentCountryFlagMap = new HashMap<>();
	private static Gson gson = new Gson();
	private static String datafile = "continents.txt"; // default file used for testing

	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.out.println("Usage: java App properties.xml");
			System.exit(0);
		}
		datafile = args[0];
		SpringApplication.run(App.class, args);
	}

	private String buildJson(String key, String value) {
		JsonObject json = new JsonObject();
		json.addProperty(key, value);
		return gson.toJson(json);
	}

	private Set<String> getContinents() {
		return continentCountryFlagMap.keySet();
	}

	private Set<String> getCountries() {
		return countryFlagMap.keySet();
	}

	private String getFlag(String country) {
		return countryFlagMap.get(country);
	}

	@RequestMapping(value = "/flags", method = RequestMethod.GET)
	@ResponseBody
	private ResponseEntity<String> getFlags() {
		return getFlags(null);
	}

	@RequestMapping(value = "/flags/{id}", method = RequestMethod.GET)
	@ResponseBody
	private ResponseEntity<String> getFlags(@PathVariable("id") String id) {
		if (id == null) {
			return ResponseEntity.ok(gson.toJson(getFlagsByContinent())); // all flags grouped by continent
		} else {
			if (getCountries().contains(id)) {
				return ResponseEntity.ok(buildJson(id, getFlag(id))); // flag for the country
			} else if (getContinents().contains(id)) {
				return ResponseEntity.ok(gson.toJson(getFlagsByContinent(id))); // all flags for the continent
			}
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(buildJson("error", "Unknown country or continent"));
	}

	private Map<String, Map<String, String>> getFlagsByContinent() {
		return continentCountryFlagMap;
	}

	private Map<String, String> getFlagsByContinent(String continent) {
		return continentCountryFlagMap.get(continent);
	}

	/**
	 * Loads the JSON data using GSON reader and the entity classes
	 * 
	 * @param dataFile
	 *            The JSON data file to read data from.
	 * @throws IOException
	 *             If there is any error reading the data file.
	 */
	private void initData(String dataFile) throws IOException {
		logger.info("Initializing the store using data from {} ...", dataFile);
		try (JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(dataFile), "UTF-8"))) {
			Continent[] continents = gson.fromJson(reader, Continent[].class);
			for (Continent continent : continents) {
				continent.countries.forEach(country -> {
					if (!continentCountryFlagMap.containsKey(continent.continent)) {
						continentCountryFlagMap.put(continent.continent, new HashMap<>());
					}
					continentCountryFlagMap.get(continent.continent).put(country.name, country.flag);
					countryFlagMap.put(country.name, country.flag);
					logger.debug("Initalizing data for {} {} {}", continent.continent, country.name, country.flag);
				});
			}
		}
	}

	@Override
	public void onApplicationEvent(final ApplicationReadyEvent event) {
		try {
			initData(datafile);
		} catch (IOException e) {
			throw new RuntimeException("Data loading failed", e);
		}
	}

}
