package service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;

import model.PropertyCount;
import service.interfaces.*;

public class AhoCorasickScanner implements PropertyScanner {
	

	@Override
	public Set<String> searchForUsages(PropertyCount pCount, String root) {
		Set<String> keysFound = null;
		Set<String> includedProperties = new HashSet<>();

		List<String> filePaths = retrieveFilePaths(root);
		String[] keyArray = pCount.getKeySet().toArray(new String[0]); 
	 
		
		Trie trie = Trie.builder().addKeywords(keyArray).build();
  		for (String fPath : filePaths) {
//			System.out.println("IN FILE PATH: " + fPath + ": ");
			String text = retrieveTextFromFile(fPath);
			Collection<Emit> emits = trie.parseText(text);
			keysFound = emits.stream().map(key -> key.getKeyword()).collect(Collectors.toSet());
			includedProperties.addAll(keysFound);
//			System.out.println(keysFound);
  		} 
		return includedProperties;

	}

	 
	private List<String> retrieveFilePaths(String root) {
		Path path = Paths.get(root);
		List<String> filePaths = new ArrayList<>();

		try {
			filePaths = Files.walk(path).parallel().filter(f -> f.toString().endsWith(".java") || f.toString().endsWith(".jsf")
					|| f.toString().endsWith(".jsff")).map(Path::toString).collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException("Error in retrieving files");
		}
		return filePaths;
	}

	 
	private static String retrieveTextFromFile(String fileName) {
		String text;
		try {
			text = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
		return text;
	}
}
