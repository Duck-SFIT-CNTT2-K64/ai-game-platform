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
 * Loads embedded UI fonts once. 
 * VT323 is the new project-wide font.
 */
public final class AppFonts {

	private static final String VT323_RESOURCE = "/font/VT323-Regular.ttf";
	private static String vt323Family = "Segoe UI";

	static {
		try (InputStream is = AppFonts.class.getResourceAsStream(VT323_RESOURCE)) {
			if (is != null) {
				Font loaded = Font.loadFont(is, 12);
				if (loaded != null) {
					vt323Family = loaded.getFamily();
				}
			}
		} catch (IOException ignored) {}
	}

	private AppFonts() {
	}

	public static String getFamily() {
		return vt323Family;
	}

	public static Font vt323(double size) {
		return Font.font(vt323Family, FontWeight.NORMAL, size);
	}

	/** Applies VT323 font to common text-bearing nodes in a subtree. */
	public static void applyTo(Parent root) {
		if (root == null) {
			return;
		}
		for (Node node : root.lookupAll("*")) {
			if (node instanceof Text t) {
				double size = t.getFont() == null ? 14 : t.getFont().getSize();
				t.setFont(vt323(size * 1.2)); // VT323 often looks smaller, scale up slightly
			} else if (node instanceof Labeled l) {
				double size = l.getFont() == null ? 14 : l.getFont().getSize();
				l.setFont(vt323(size * 1.2));
			} else if (node instanceof TextInputControl input) {
				double size = input.getFont() == null ? 14 : input.getFont().getSize();
				input.setFont(vt323(size * 1.2));
			}
		}
	}
}
