"""
Fix opaque black backgrounds in MainMenu PNG files.
Makes near-black pixels (R<12, G<12, B<12) fully transparent.
This allows proper layering in JavaFX: Water → Duck → Bamboo Frame.
"""
from PIL import Image
import os

BASE = r"d:\nam_3_ki_2\tri_tue_nhan_tao\ai-game-platform\src\main\resources\image\pixel_MainMenu"
THRESHOLD = 12  # Very conservative: only truly black pixels


def fix_black_to_transparent(filepath):
    """Remove opaque black background from PNG, making it transparent."""
    img = Image.open(filepath).convert('RGBA')
    pixels = img.load()
    w, h = img.size
    count = 0

    for y in range(h):
        for x in range(w):
            r, g, b, a = pixels[x, y]
            if r < THRESHOLD and g < THRESHOLD and b < THRESHOLD and a > 0:
                pixels[x, y] = (0, 0, 0, 0)
                count += 1

    img.save(filepath)
    pct = (count / (w * h)) * 100
    print(f"  Fixed: {os.path.basename(filepath)} — {count} pixels ({pct:.1f}%) made transparent")


# Fix Grass_mainmenu.png (bamboo frame — center and edges should be transparent)
print("=== Fixing Grass_mainmenu.png ===")
fix_black_to_transparent(os.path.join(BASE, "Grass_mainmenu.png"))

# Fix Water_1..4.png (pond — black area around the pond should be transparent)
for i in range(1, 5):
    print(f"\n=== Fixing Water_{i}.png ===")
    fix_black_to_transparent(os.path.join(BASE, f"Water_{i}.png"))

# Verify fix
print("\n=== Verification ===")
img = Image.open(os.path.join(BASE, "Grass_mainmenu.png")).convert('RGBA')
print(f"Grass corner pixel: {img.getpixel((0, 0))}  (should be alpha=0)")
print(f"Grass center pixel: {img.getpixel((img.size[0]//2, img.size[1]//2))}  (should be alpha=0)")

img2 = Image.open(os.path.join(BASE, "Water_1.png")).convert('RGBA')
print(f"Water corner pixel: {img2.getpixel((0, 0))}  (should be alpha=0)")
print(f"Water center pixel: {img2.getpixel((img2.size[0]//2, img2.size[1]//2))}  (should be non-zero alpha)")

print("\nDone! All black backgrounds removed.")
