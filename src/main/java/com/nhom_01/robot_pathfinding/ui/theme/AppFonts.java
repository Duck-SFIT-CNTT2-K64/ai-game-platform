package com.nhom_01.robot_pathfinding.ui.theme;

import java.io.IOException;
import java.io.InputStream;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextInputControl;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Loads embedded UI fonts once. Jersey 10’s registered family name is not the file name
 * (typically {@code "Jersey 10"}), so we resolve it from the loaded {@link Font}.
 */
public final class AppFonts {

	private static final String JERSEY_RESOURCE = "/font/Jersey10-Regular.ttf";
	private static String jerseyFamily = "Segoe UI";

	static {
		try (InputStream is = AppFonts.class.getResourceAsStream(JERSEY_RESOURCE)) {
			if (is != null) {
				Font loaded = Font.loadFont(is, 12);
				if (loaded != null) {
					jerseyFamily = loaded.getFamily();
				}
			}
		} catch (IOException ignored) {
			// keep fallback family
		}
	}

	private AppFonts() {
	}

	/** Family name JavaFX registered for {@link #JERSEY_RESOURCE} (for CSS / {@link Font#font}). */
	public static String getJerseyFamily() {
		return jerseyFamily;
	}

	/** Jersey 10 is a single regular face; avoid BOLD so JavaFX does not substitute incorrectly. */
	public static Font jersey(double size) {
		return Font.font(jerseyFamily, FontWeight.NORMAL, size);
	}

	/** Applies Jersey font to common text-bearing nodes in a subtree. */
	public static void applyTo(Parent root) {
		if (root == null) {
			return;
		}
		for (Node node : root.lookupAll("*")) {
			if (node instanceof Text t) {
				double size = t.getFont() == null ? 14 : t.getFont().getSize();
				t.setFont(jersey(size));
			} else if (node instanceof Labeled l) {
				double size = l.getFont() == null ? 14 : l.getFont().getSize();
				l.setFont(jersey(size));
			} else if (node instanceof TextInputControl input) {
				double size = input.getFont() == null ? 14 : input.getFont().getSize();
				input.setFont(jersey(size));
			}
		}
	}
}
