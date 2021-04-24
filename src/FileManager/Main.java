package FileManager;

import FileManager.GUI;

import java.io.File;

public class Main {

	public static void main(String[] args) {
		GUI gui = new GUI();
		gui.go();

		File[] paths = File.listRoots();
		for (File path:paths) {
			System.out.println(path);
		}
	}

}
