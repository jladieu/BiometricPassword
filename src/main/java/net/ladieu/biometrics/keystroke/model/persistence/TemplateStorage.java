package net.ladieu.biometrics.keystroke.model.persistence;

import java.io.IOException;
import java.util.List;

import net.ladieu.biometrics.keystroke.model.KeystrokeSequence;

public interface TemplateStorage {

	List<KeystrokeSequence> getStoredTemplate(String userName)
			throws IOException;

	void saveTemplate(String userName, List<KeystrokeSequence> template);
}
