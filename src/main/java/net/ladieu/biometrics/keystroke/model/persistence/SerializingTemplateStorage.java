package net.ladieu.biometrics.keystroke.model.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import net.ladieu.biometrics.keystroke.model.KeystrokeSequence;

public class SerializingTemplateStorage implements TemplateStorage {

	public SerializingTemplateStorage() {
		super();
	}

	private File createFileHandle(String userName) {
		return new File(userName + ".template");
	}

	@SuppressWarnings("unchecked")
	public List<KeystrokeSequence> getStoredTemplate(String userName)
			throws IOException {
		ObjectInputStream objectInput = null;
		List<KeystrokeSequence> result = null;
		try {
			FileInputStream fileInputStream = new FileInputStream(
					createFileHandle(userName));

			objectInput = new ObjectInputStream(fileInputStream);

			result = (List<KeystrokeSequence>) objectInput.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return result;
	}

	public void saveTemplate(String userName, List<KeystrokeSequence> template) {
		ObjectOutputStream objectOutput = null;

		try {
			FileOutputStream fileOutputStream = new FileOutputStream(
					createFileHandle(userName));

			objectOutput = new ObjectOutputStream(fileOutputStream);

			objectOutput.writeObject(template);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != objectOutput) {
				try {
					objectOutput.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
